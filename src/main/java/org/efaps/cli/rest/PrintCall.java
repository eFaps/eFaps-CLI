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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
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
import org.efaps.json.reply.ErrorReply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class PrintCall
    extends AbstractCall
{

    /**
     * Instantiates a new prints the call.
     *
     * @param _environment the _environment
     */
    public PrintCall(final Environment _environment)
    {
        super(_environment, "eql", "print");
    }

    /**
     * Compile the target in the server.
     *
     * @param _eql the _eql
     * @param _exportFormat the _export format
     * @param _fileName the _file name
     * @return the string
     */
    public String execute(final String _eql,
                          final ExportFormat _exportFormat,
                          final String _fileName)
    {
        init();
        final String fileName = StringUtils.isEmpty(_fileName) ? "export" : _fileName;

        final StringBuilder ret = new StringBuilder();

        final Response response = getWebTarget().queryParam("origin", "eFaps-CLI")
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
                                length = val.getKey().length() > String.valueOf(val.getValue()).length()
                                                ? val.getKey().length()
                                                : String.valueOf(val.getValue()).length();
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
                } else if (obj instanceof ErrorReply) {
                    writer = new StringWriter();
                    writer.append(getErrorReply(response, (ErrorReply) obj));
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
}
