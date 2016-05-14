/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */


package org.efaps.cli.rest;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.efaps.cli.utils.ExportFormat;
import org.efaps.dataexporter.DataExporter;
import org.efaps.dataexporter.model.LineNumberColumn;
import org.efaps.dataexporter.model.Row;
import org.efaps.dataexporter.model.StringColumn;
import org.efaps.dataexporter.output.csv.CsvExporter;
import org.efaps.dataexporter.output.text.TextExporter;
import org.efaps.dataexporter.output.texttable.TextTableExporter;
import org.efaps.dataexporter.output.xml.XmlExporter;
import org.efaps.json.AbstractEFapsJSON;
import org.efaps.json.index.SearchResult;
import org.efaps.json.index.SearchResult.Element;
import org.efaps.json.reply.ErrorReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class SearchCall
    extends AbstractCall
{
    /** The Constant LOG. */
    private static final  Logger LOG = LoggerFactory.getLogger(SearchCall.class);

    /**
     * Instantiates a new search call.
     *
     * @param _environment the environment
     */
    public SearchCall(final Environment _environment)
    {
        super(_environment, "search");
    }

    /**
     * Execute.
     *
     * @param _query the query
     * @param _exportFormat the _export format
     * @param _fileName the _file name
     * @return the string
     */
    public String execute(final String _query,
                          final ExportFormat _exportFormat,
                          final String _fileName)
    {
        init();
        final String fileName = StringUtils.isEmpty(_fileName) ? "search" : _fileName;
        final Response response = getWebTarget()
                        .queryParam("query", _query)
                        .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();

        LOG.debug("Response: {}", response);
        final StringBuilder ret = new StringBuilder();
        if (MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(response.readEntity(InputStream.class)));
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final Object obj = mapper.readValue(br, AbstractEFapsJSON.class);
                if (obj instanceof ErrorReply) {
                    ret.append(getErrorReply(response, (ErrorReply) obj));
                } else if (obj instanceof SearchResult) {
                    final SearchResult result = (SearchResult) obj;
                    final DataExporter tableWriter;
                    StringWriter writer = null;
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
                            lineNumberCol.setWidth(lineNumberCol.format(result.getElements().size()).length());
                            tableWriter.addColumns(lineNumberCol);
                            break;
                    }

                    tableWriter.addColumns(new StringColumn("OID", 20), new StringColumn("Text", 150));
                    for (final Element element : result.getElements()) {
                        final Row row = new Row();
                        row.add(element.getOid(), element.getText());
                        tableWriter.addRows(row);
                    }
                    if (writer != null) {
                        ret.append(writer);
                    }
                } else {
                    ret.append(obj);
                }
            } catch (final IOException e) {
                ret.append(response.getStatusInfo().toString()).append(e);
            }
        } else if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret.append(response.readEntity(String.class));
        }
        if (ret.length() == 0) {
            ret.append(response.getStatusInfo().toString());
        }
        return ret.toString();
    }
}
