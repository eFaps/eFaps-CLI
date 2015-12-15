/*
 * Copyright 2003 - 2015 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.cli.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.efaps.cli.utils.CLISettings;
import org.efaps.cli.utils.ExportFormat;
import org.efaps.dataexporter.DataExporter;
import org.efaps.dataexporter.model.Column;
import org.efaps.dataexporter.model.LineNumberColumn;
import org.efaps.dataexporter.model.NumberColumn;
import org.efaps.dataexporter.model.Row;
import org.efaps.dataexporter.model.StringColumn;
import org.efaps.dataexporter.output.csv.CsvExporter;
import org.efaps.dataexporter.output.text.TextExporter;
import org.efaps.dataexporter.output.texttable.TextTableExporter;
import org.efaps.dataexporter.output.tree.TreeExporter;
import org.efaps.dataexporter.output.xml.XmlExporter;
import org.efaps.json.AbstractEFapsJSON;
import org.efaps.json.ci.AbstractCI;
import org.efaps.json.ci.Attribute;
import org.efaps.json.ci.Type;
import org.efaps.json.data.AbstractValue;
import org.efaps.json.data.DataList;
import org.efaps.json.data.DateTimeValue;
import org.efaps.json.data.DecimalValue;
import org.efaps.json.data.LongValue;
import org.efaps.json.data.ObjectData;
import org.efaps.json.data.StringListValue;
import org.efaps.json.data.StringValue;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RestClient
{

    /**
     * Client that makes the actual connection.
     */
    private Client client;

    /**
     * Url to be used in the request by this client.
     */
    private WebTarget webTarget;

    private final Environment environment;

    private boolean initialized;

    /**
     * @param _url url for this client.
     */
    public RestClient(final Environment _environment)
    {
        this.environment = _environment;
    }

    /**
     * Initialize the client.
     */
    public void init()
    {
        if (!this.initialized) {
            final String pwd = (String) this.environment.getVariable(CLISettings.PWD).getValue();
            final String user = (String) this.environment.getVariable(CLISettings.USER).getValue();
            final String url = (String) this.environment.getVariable(CLISettings.URL).getValue();
            final ClientConfig clientConfig = new ClientConfig();

            final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pwd);
            clientConfig.register(feature).register(MultiPartFeature.class);

            this.client = ClientBuilder.newClient(clientConfig);
            this.webTarget = this.client.target(url);
        }
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String compile(final String _target)
    {
        init();
        final WebTarget resourceWebTarget = this.webTarget.path("compile");

        final Response response = resourceWebTarget.queryParam("type", _target).request(MediaType.TEXT_PLAIN_TYPE)
                        .get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String update(final String _eql)
    {
        init();
        final WebTarget resourceWebTarget = this.webTarget.path("eql").path("update");
        final Response response = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE).get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String delete(final String _eql)
    {
        init();
        final WebTarget resourceWebTarget = this.webTarget.path("eql").path("delete");
        final Response response = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE).get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String insert(final String _eql)
    {
        init();
        final WebTarget resourceWebTarget = this.webTarget.path("eql").path("insert");
        final Response response = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE).get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String print(final String _eql,
                        final ExportFormat _exportFormat,
                        final String _fileName)
    {
        init();
        final String fileName = StringUtils.isEmpty(_fileName) ? "export" : _fileName;

        final StringBuilder ret = new StringBuilder();
        final WebTarget resourceWebTarget = this.webTarget.path("eql").path("print");
        final Response response = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();

        if (MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {

            final BufferedReader br = new BufferedReader(new InputStreamReader(response.readEntity(InputStream.class)));

            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JodaModule());

            try {
                final Object obj = mapper.readValue(br, AbstractEFapsJSON.class);
                StringWriter writer = null;
                if (obj instanceof AbstractCI) {
                    final AbstractCI<?> ciObject = (AbstractCI<?>) obj;
                    writer = new StringWriter();
                    final TreeExporter treeWriter = new TreeExporter(writer);

                    final Row root = new Row(ciObject.getName());
                    root.addChild(new Row("Nature: Type"));
                    root.addChild(new Row("UUID: " + ciObject.getUUID()));
                    root.addChild(new Row("ID: " + ciObject.getId()));
                    final Row attrNode = new Row("Attributes");
                    root.addChild(attrNode);
                    for (final Attribute attr : ((Type) ciObject).getAttributes()) {
                        attrNode.addChild(new Row(attr.getName()));
                    }
                    treeWriter.addRows(root);
                } else if (obj instanceof DataList) {
                    final DataList tmp = (DataList) obj;
                    DataExporter tableWriter;
                    boolean permitNUll = true;
                    switch (_exportFormat) {
                        case CSV:
                            tableWriter = new CsvExporter(new FileOutputStream(fileName + ".csv"));
                            ret.append("Exported to CSV.");
                            break;
                        case TXT:
                            tableWriter = new TextExporter(new FileOutputStream(fileName + ".txt"));
                            ret.append("Exported to txt.");
                            break;
                        case XML:
                            tableWriter = new XmlExporter(new FileOutputStream(fileName + ".xml"));
                            ret.append("Exported to xml.");
                            break;
                        default:
                            writer = new StringWriter();
                            tableWriter = new TextTableExporter(writer);
                            final LineNumberColumn lineNumberCol = new LineNumberColumn("", 1);
                            lineNumberCol.setWidth(lineNumberCol.format(tmp.size()).length());
                            tableWriter.addColumns(lineNumberCol);
                            permitNUll = false;
                            break;
                    }

                    final Map<String, Column> key2Column = new LinkedHashMap<>();
                    for (final ObjectData objData : tmp) {
                        for (final AbstractValue<?> val : objData.getValues()) {
                            int length = 0;
                            if (key2Column.containsKey(val.getKey())) {
                                length = String.valueOf(val.getValue()).length() + 2;
                            } else {
                                if (val instanceof StringValue) {
                                    key2Column.put(val.getKey(), new StringColumn(val.getKey()));
                                } else if (val instanceof DateTimeValue) {
                                    key2Column.put(val.getKey(), new StringColumn(val.getKey()));
                                } else if (val instanceof DecimalValue) {
                                    key2Column.put(val.getKey(), new NumberColumn(val.getKey(), 1, 2));
                                } else if (val instanceof LongValue) {
                                    key2Column.put(val.getKey(), new NumberColumn(val.getKey(), 1, 0));
                                } else {
                                    key2Column.put(val.getKey(), new StringColumn(val.getKey()));
                                }
                                length = val.getKey().length() > String.valueOf(val.getValue()).length() ? val
                                                .getKey().length() :
                                                String.valueOf(val.getValue()).length();
                            }
                            if (length > key2Column.get(val.getKey()).getWidth()) {
                                key2Column.get(val.getKey()).setWidth(length);
                            }
                        }
                    }
                    tableWriter.addColumns(key2Column.values().toArray(new Column[key2Column.size()]));

                    for (final ObjectData objData : tmp) {
                        final Row row = new Row();
                        for (final AbstractValue<?> val : objData.getValues()) {
                            Object value = val.getValue();
                            if (!permitNUll) {
                                if (value instanceof String && ((String) value).isEmpty()) {
                                    value = " ";
                                }
                            }
                            if (val instanceof StringListValue) {
                                final StringBuilder bldr = new StringBuilder();
                                boolean first = true;
                                for (final String strVal : ((StringListValue) val).getValue()) {
                                    if (first) {
                                        first = false;
                                    } else {
                                        bldr.append("\n");
                                    }
                                    bldr.append(strVal);
                               }
                               value = bldr.toString();
                            }
                            row.addCellValue(value);
                        }
                        tableWriter.addRows(row);
                    }
                }
                if (writer != null) {
                    ret.append(writer);
                }
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret.append(response.readEntity(String.class));
        } else {
            ret.append(response.getStatusInfo().toString());
        }
        return ret.toString();
    }

    /**
     * @param _files
     * @param _revFile
     * @throws Exception
     */
    public String post(final List<File> _files,
                       final File _revFile)
    {
        init();
        final Map<String, String[]> fileInfo = new HashMap<>();
        if (_revFile != null) {
            try {
                final BufferedReader br = new BufferedReader(new FileReader(_revFile));
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] arr = line.split(" ");
                    if (arr.length > 2) {
                        fileInfo.put(arr[0], new String[] { arr[1], arr[2] });
                    }
                }
                br.close();
            } catch (final FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        final FormDataMultiPart multiPart = new FormDataMultiPart();
        for (final File file : _files) {
            final FileDataBodyPart part = new FileDataBodyPart("eFaps_File", file);
            multiPart.bodyPart(part);
            if (_revFile == null) {
                final String[] info = getFileInformation(file);
                multiPart.field("eFaps_Revision", info[0]);
                multiPart.field("eFaps_Date", info[1]);
            } else {
                final String[] info = fileInfo.get(file.getName());
                if (info == null) {
                    multiPart.field("eFaps_Revision", "");
                    multiPart.field("eFaps_Date", "");
                } else {
                    multiPart.field("eFaps_Revision", info[0]);
                    multiPart.field("eFaps_Date", info[1]);
                }
            }
        }
        final Response response = this.webTarget.path("update").request()
                        .post(Entity.entity(multiPart, multiPart.getMediaType()));

        return response.getStatusInfo().toString();
    }

    protected String[] getFileInformation(final File _file)
    {
        final String[] ret = new String[2];

        try {
            final Repository repo = new FileRepository(evalGitDir(_file));

            final ObjectId lastCommitId = repo.resolve(Constants.HEAD);

            final PlotCommitList<PlotLane> plotCommitList = new PlotCommitList<PlotLane>();
            final PlotWalk revWalk = new PlotWalk(repo);

            final RevCommit root = revWalk.parseCommit(lastCommitId);
            revWalk.markStart(root);
            revWalk.setTreeFilter(AndTreeFilter.create(
                            PathFilter.create(_file.getPath().replaceFirst(repo.getWorkTree().getPath() + "/", "")),
                            TreeFilter.ANY_DIFF));
            plotCommitList.source(revWalk);
            plotCommitList.fillTo(2);
            final PlotCommit<PlotLane> commit = plotCommitList.get(0);
            if (commit != null) {
                final PersonIdent authorIdent = commit.getAuthorIdent();
                final Date authorDate = authorIdent.getWhen();
                final TimeZone authorTimeZone = authorIdent.getTimeZone();
                final DateTime dateTime = new DateTime(authorDate.getTime(), DateTimeZone.forTimeZone(authorTimeZone));
                ret[1] = dateTime.toString();
                ret[0] = commit.getId().getName();
            } else {
                ret[1] = new DateTime().toString();
                ret[0] = "UNKNOWN";
            }
        } catch (RevisionSyntaxException | IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param _file
     * @return
     */
    protected File evalGitDir(final File _file)
    {
        File ret = null;
        File parent = _file.getParentFile();
        ;
        while (parent != null) {
            ret = new File(parent, ".git");
            if (ret.exists()) {
                break;
            } else {
                parent = parent.getParentFile();
            }
        }
        return ret;
    }
}
