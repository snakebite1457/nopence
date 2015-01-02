package org.meyerlab.nopence.jm_prta_parser.util.model;

import org.meyerlab.nopence.jm_prta_parser.util.Constants;
import org.meyerlab.nopence.jm_prta_parser.util.Helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Dennis Meyer
 */
public class OutputFile {

    private File _outputFile;
    private ArrayList<Sequence> _sequenceBuffer;
    private int _createdSequences;
    private int _numBuffSeq;

    private Sequence _curSequence;
    private long _lastSeqNumber;
    private int _maxSequences;

    private boolean _reachSeqLimit;

    /**
     * @param outputFile   the file where the converted attributes will be
     *                     written. If this file exists it will be deleted
     * @param maxSequences the max sequences for this output file.
     * @throws IOException
     */
    public OutputFile(File outputFile, int maxSequences) throws IOException {
        _outputFile = outputFile;
        _maxSequences = maxSequences;

        _sequenceBuffer = new ArrayList<>();
        _createdSequences = 0;

        _curSequence = null;
        _lastSeqNumber = 1;
        _reachSeqLimit = false;

        int calcBufferSize = maxSequences / 100;
        if (calcBufferSize > 10 && calcBufferSize < 50) {
            _numBuffSeq = calcBufferSize;
        } else {
            _numBuffSeq = Constants.NUM_DEFAULT_SEQ_BUFFER_SIZE;
        }

        Helper.createFile(_outputFile);
    }

    public void addInstance(Instance inst, long personId) {

        if (_curSequence == null || !_curSequence.equalPersonId(personId)) {

            // Check if max sequences limit is reached
            boolean canCreateSeq = _createdSequences <= _maxSequences;

            if (_curSequence != null) {
                // Write instance into output file
                writeSequence(_curSequence, !canCreateSeq);
            }

            if (canCreateSeq) {
                _curSequence = new Sequence(_lastSeqNumber++, personId);
                _createdSequences++;
            } else {
                _reachSeqLimit = true;
                return;
            }
        }

        _curSequence.addInstance(inst);
    }

    public boolean seqLimitReached() {
        return _reachSeqLimit;
    }

    private void writeSequence(Sequence seq, boolean force) {
        if (_sequenceBuffer.size() <= _numBuffSeq && !force) {
            _sequenceBuffer.add(seq);
        } else {
            try {
                FileWriter fileWriter = new FileWriter(_outputFile, true);
                try {
                    for (Sequence sequence : _sequenceBuffer) {
                        fileWriter.write(sequence.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    fileWriter.flush();
                    fileWriter.close();
                    _sequenceBuffer.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
