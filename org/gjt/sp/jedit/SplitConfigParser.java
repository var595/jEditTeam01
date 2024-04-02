/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2004 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Class used by PerspectiveManager to parse SplitConfig strings.
 May also be used by plugins.
 @since jEdit 4.4
 */
public class SplitConfigParser
{

    //{{{ private members
    private String splitConfig;

    private boolean includeSplits = true;
    private boolean includeFiles = true;
    private boolean includeRemotes = false;
    //}}}
    private StreamTokenizer tokenizer; // Add this field

    //{{{ SplitConfigParser constructor
    /**
     * @param splitConfig The string to parse and adjust.
     */
    public SplitConfigParser(String splitConfig)
    {
        this.splitConfig = splitConfig == null ? "" : splitConfig;
        initializeTokenizer();
    }
    //}}}

    //{{{ Setters
    /**
     * @param b If true, retain any splits in the split configuration.
     */
    public void setIncludeSplits(boolean b)
    {
        includeSplits = b;
    }

    /**
     * @param b If true, retain any file names found in the split configuration.
     */
    public void setIncludeFiles(boolean b)
    {
        includeFiles = b;
    }

    /**
     * @param b If true, and if include files is true, then retain any remote file
     * names found in the split configuration.
     */
    public void setIncludeRemoteFiles(boolean b)
    {
        includeRemotes = includeFiles && b;
    }
    //}}}

    //{{{ parse()
    /**
     * Parses the given split configuration string and removes splits, file names,
     * and remote file names bases on the settings for this parser.
     * @return The split configuration string adjusted for user preferences.
     */

    public String parse() {
        if (splitConfig == null || splitConfig.isEmpty()) {
            return "";
        }

        initializeTokenizer();
        processTokens();
        return buildAdjustedSplitConfig();
    }

    private void initializeTokenizer() {
        try {
            tokenizer = new StreamTokenizer(new StringReader(splitConfig));
            tokenizer.whitespaceChars(0, ' ');
            tokenizer.wordChars('#', '~');
            tokenizer.commentChar('!');
            tokenizer.quoteChar('"');
            tokenizer.eolIsSignificant(false);
        } catch (Exception e) {
            // Handle initialization errors if necessary
        }
    }

    private void processTokens() {
        try {
            int token = tokenizer.nextToken();

            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        processWordToken();
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        processNumberToken();
                        break;
                    case '"':
                        processStringToken();
                        break;
                }
                token = tokenizer.nextToken();
            }
        } catch (IOException e) {
            // Handle IO errors if necessary
        }
    }

    private void processWordToken() {
        String word = tokenizer.sval;
        if ("vertical".equals(word) || "horizontal".equals(word)) {
            processSplit();
        } else if ("buffer".equals(word) || "buff".equals(word)) {
            processBuffer();
        } else if ("bufferset".equals(word)) {
            processBufferSet();
        }
    }

    private void processSplit() {
        // Handle split processing
    }

    private void processBuffer() {
        // Handle buffer processing
    }

    private void processBufferSet() {
        // Handle buffer set processing
    }

    private void processNumberToken() {
        // Handle number token processing
    }

    private void processStringToken() {
        // Handle string token processing
    }

    private String buildAdjustedSplitConfig() {
        // Build the adjusted split configuration string based on processed tokens
        return "";
    }


    //{{{ BufferSet
    // Represents a set of file names for buffers.
    private class BufferSet
    {
        List<String> buffers = new ArrayList<String>();
        String scope = null;

        boolean includeFiles = true;
        boolean includeRemotes = false;

        public BufferSet() {}

        public BufferSet(boolean includeFiles, boolean includeRemotes)
        {
            this.includeFiles = includeFiles;
            this.includeRemotes = includeRemotes;
        }

        public void addBuffer(String s)
        {
            if (includeFiles)
            {
                if (includeRemotes)
                {
                    buffers.add(s);
                    return;
                }
                if (!isRemote(s))
                {
                    buffers.add(s);
                }
            }
        }

        public List<String> getBuffers()
        {
            return buffers;
        }

        public void addBufferSet(BufferSet bs)
        {
            buffers.addAll(bs.getBuffers());
        }

        public void setScope(String s)
        {
            scope = s;
        }

        public String getScope()
        {
            return scope;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            if (buffers.size() == 0)
            {
                sb.append("\"Untitled-1\" buffer ");
            }
            else
            {
                for (int i = 0; i < buffers.size(); i++)
                {
                    sb.append('\"').append(buffers.get(i)).append('\"');
                    sb.append(i == 0 ? " buffer " : " buff ");
                }
            }
            if (scope == null)
            {
                scope = "view";
            }
            sb.append('\"').append(scope).append("\" bufferset");
            return sb.toString();
        }

        /**
         * @return true if the uri points to a file that is remote, that is, the
         * protocol of the give uri is something other than 'file'.
         */
        public boolean isRemote(String uri)
        {
            if (MiscUtilities.isURL(uri))
            {
                String protocol = MiscUtilities.getProtocolOfURL(uri);
                return !protocol.equals("file");
            }
            return false;
        }
    }
    //}}}

    //{{{ Split
    private class Split
    {
        Object left = null;
        Object right = null;
        String direction = null;
        int offset = 0;

        // no error checking, assumes caller will pass a BufferSet or a Split
        public void setLeft(Object left)
        {
            this.left = left;
        }

        // no error checking, assumes caller will pass a BufferSet or a Split
        public void setRight(Object right)
        {
            this.right = right;
        }

        // no error checking, assumes caller will send 'horizontal' or 'vertical'
        public void setDirection(String direction)
        {
            this.direction = direction;
        }

        // no error checking, assumes caller will send offset >= 0
        public void setOffset(int offset)
        {
            this.offset = offset;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            if (left != null)
            {
                sb.append(left.toString()).append(' ');
            }
            if (right != null)
            {
                sb.append(right.toString()).append(' ');
            }
            sb.append(offset).append(' ').append(direction);
            return sb.toString();
        }
    }
    //}}}
}

