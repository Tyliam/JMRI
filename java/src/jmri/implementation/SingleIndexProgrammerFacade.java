// SingleIndexProgrammerFacade.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.PowerManager;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.jmrix.AbstractProgrammer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Programmer facade, at this point just an example.
 * <p>
 * This one isn't particularly useful. It imagines
 * that CVs from 0 to "top" can be addressed
 * directly. (Top being a power of two)
 * Above the top CV, the upper bits are written to 
 * a specific CV, followed by an operation
 * with just the lower bits. This works
 * for CV addresses up to some known "max" value. 
 *
 * @author      Bob Jacobsen  Copyright (C) 2013
 * @version	$Revision$
 */
public class SingleIndexProgrammerFacade extends AbstractProgrammer implements ProgListener {

    /**
     * @param top CVs above this use the indirect method
     * @param addrCV  CV to which the high value is to be written
     * @param max Maximum CV that can be accessed this way
     */
    public SingleIndexProgrammerFacade(Programmer prog, int top, int addrCV, int max) {
        this.prog = prog;
        this.top = top;
        this.addrCV = addrCV;
        this.max = max;
        
        if (prog.getMaxCvAddr()<addrCV) log.error("Underlying programmer can't access index CV: "+prog.getMaxCvAddr()+" "+addrCV);
        if (prog.getMaxCvAddr()<top) log.error("Underlying programmer doesn't support full range: "+prog.getMaxCvAddr()+" "+top);
    }

    Programmer prog;
    
    int top;
    int addrCV;
    int max;

    /**
     * Switch to a new programming mode by reflecting to the
     * underlying programmer
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
    public void setMode(int mode) {
        prog.setMode(mode);
    }

    /**
     * Signifies mode's available
     * @param mode
     * @return True if supported by underlying programmer
     */
    public boolean hasMode(int mode) {
        return prog.hasMode(mode);
    }
    public int getMode() { return prog.getMode(); }

    public boolean getCanRead() {
        return prog.getCanRead();
    }

    public int getMaxCvAddr() { 
        return max;
    }

    // notify property listeners - see AbstractProgrammer for more

    @SuppressWarnings("unchecked")
	protected void notifyPropertyChange(String name, int oldval, int newval) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this) {
            v = (Vector<PropertyChangeListener>) propListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, name, Integer.valueOf(oldval), Integer.valueOf(newval)));
        }
    }

    // members for handling the programmer interface

    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _cv = CV;
        _val = val;
        useProgrammer(p);
        if (CV <= top) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(CV, val, this);
        } else {
            // write index first
            state = ProgState.FINISHWRITE;
            prog.writeCV(addrCV, CV/top, this);
        }
    }

    synchronized public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        _cv = CV;
        useProgrammer(p);
        if (CV <= top) {
            state = ProgState.PROGRAMMING;
            prog.readCV(CV, this);
        } else {
            // write index first
            state = ProgState.FINISHREAD;
            prog.writeCV(addrCV, CV/top, this);
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) log.info("programmer already in use by "+_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            _usingProgrammer = p;
            return;
        }
    }

    enum ProgState { PROGRAMMING, FINISHREAD, FINISHWRITE, NOTPROGRAMMING }
    ProgState state = ProgState.NOTPROGRAMMING;
    
    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        if (state != ProgState.NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) log.debug("timeout!");
            state = ProgState.NOTPROGRAMMING;
            programmingOpReply(_val, jmri.ProgListener.FailedTimeout);
        }
    }
    
    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) log.debug("notifyProgListenerEnd value "+value+" status "+status);
        
        if (_usingProgrammer == null) log.error("No listener to notify");

        switch (state) {
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(value, status);
                break;
            case FINISHREAD:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.readCV(_cv%top, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(_cv%top, _val, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
                }
                break;
            default:
                log.error("Unexpected state on reply: "+state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                
        }
    }

    static Logger log = LoggerFactory.getLogger(SingleIndexProgrammerFacade.class.getName());

}

/* @(#)SprogProgrammer.java */