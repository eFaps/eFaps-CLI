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
package org.efaps.cli;

import java.util.Iterator;
import java.util.List;

import org.efaps.cli.rest.CINameProviderCall;
import org.efaps.eql.ide.contentassist.EQLProposals;
import org.efaps.eql.ide.contentassist.ICINameProvider;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.ShellCommandParamSpec;
import de.raysha.lib.jsimpleshell.annotation.Inject;
import de.raysha.lib.jsimpleshell.completer.AbstractCandidatesChooser;
import de.raysha.lib.jsimpleshell.completer.filter.CandidateFilter;
import de.raysha.lib.jsimpleshell.completer.filter.CompositeCandidateFilter;

/**
 * The Class EQLCandidatesChooser.
 *
 * @author The eFaps Team
 */
public class EQLCandidatesChooser
    extends AbstractCandidatesChooser
{

    /** The input. */
    @Inject
    private Shell shell;

    /**
     * Instantiates a new EQL candidates chooser.
     */
    public EQLCandidatesChooser()
    {
        super("eql");
    }

    @Override
    public Candidates chooseCandidates(final ShellCommandParamSpec _paramSpec,
                                       final String _part)
    {
        if (EQLProposals.getCINameProviders().isEmpty()) {
            final CINameProviderCall call = new CINameProviderCall(this.shell.getEnvironment());
            final ICINameProvider provider = call.execute();
            if (provider != null) {
                EQLProposals.registerCINameProviders(provider);
            }
        }
        Candidates ret = null;
        if (responsibleFor(_paramSpec)) {
            String txt = "";
            final List<CandidateFilter> chain = ((CompositeCandidateFilter) this.shell.getCandidatesFilter())
                            .getFilterChain();
            for (final CandidateFilter filter : chain) {
                if (filter instanceof EQLFilter) {
                    txt = ((EQLFilter) filter).getPart();
                    break;
                }
            }
            final List<String> proposals = EQLProposals.getProposalList(txt);
            if (!_part.isEmpty()) {
                final Iterator<String> iter = proposals.iterator();
                while (iter.hasNext()) {
                    final String val = iter.next();
                    if (!val.startsWith(_part)) {
                        iter.remove();
                    }
                }
            }
            ret = new Candidates(proposals, 0);
        }
        return ret;
    }
}
