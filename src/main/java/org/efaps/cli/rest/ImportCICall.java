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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

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
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class ImportCICall
    extends AbstractCall
{
    /**
     * Instantiates a new import ci call.
     *
     * @param _environment the _environment
     */
    public ImportCICall(final Environment _environment)
    {
        super(_environment, "update");
    }

    /**
     * Post.
     *
     * @param _files the _files
     * @param _revFile the _rev file
     * @return the string
     */
    public String execute(final List<File> _files,
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
        final Response response = getWebTarget().request()
                        .post(Entity.entity(multiPart, multiPart.getMediaType()));
        return response.getStatusInfo().toString();
    }

    /**
     * Gets the file information.
     *
     * @param _file the _file
     * @return the file information
     */
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
        } catch (final RevisionSyntaxException | IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Eval git dir.
     *
     * @param _file the _file
     * @return the file
     */
    protected File evalGitDir(final File _file)
    {
        File ret = null;
        File parent = _file.getParentFile();
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
