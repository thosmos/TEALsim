/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: ConstantField.java,v 1.7 2010/07/21 21:57:18 stefan Exp $ 
 * 
 */

package teal.physics.em;

import javax.vecmath.*;

import teal.sim.engine.TEngineControl;
import teal.sim.engine.TSimEngine;
import teal.physics.*;
import teal.util.*;

/**
 
 * ConstantField is an object that generates a constant field (electric or magnetic) throughout space in a given direction.
 */
public class ConstantField extends EMObject implements GeneratesE, GeneratesB {

    private static final long serialVersionUID = 3257289123621253943L;
    
    //protected TEngine theEngine;
    protected Vector3d zdir;
    protected Vector3d xdir;
    protected Vector3d position;
    protected double magnitude, magnitude_d, magnitude_derivative;
    protected double radius;
    protected double height;
    protected boolean generatingBField = true;
    protected boolean generatingEField = false;

    protected boolean isIntegrating;

    public static final int B_FIELD = 0;
    public static final int E_FIELD = 1;
    protected int fieldType = 0;

    /**
     * Creates a constant field with supplied position, direction, and magnitude.
     * @param pos The position of this constant field.
     * @param dir The direction of this constant field.
     * @param mag The magnitude of this constant field.
     */
    public ConstantField(Vector3d pos, Vector3d dir, double mag) {
        position = pos;
        dir.normalize();
        zdir = dir;
        zdir.normalize();
        magnitude = mag;
        isIntegrating = true;
        magnitude_derivative = 0.;

        Vector3d temp = new Vector3d(0, 0, 0);
        temp.cross(zdir, new Vector3d(0, 0, 1.0));

        if (temp.length() != 0.0) {
            temp.normalize();
            xdir = temp;
        } else {
            xdir = new Vector3d(1., 0, 0);
        }
        /////
        //xdir = new Vector3d(0,-1.0,0);
        //System.out.println("ConstantField zdir = " + zdir + " xdir = " + xdir);
    }

//    /**
//     * Returns a reference to theEngine.
//     * 
//     */
//    public TEngine getEngine() {
//        return theEngine;
//    }
//
//    /**
//     * Sets the model for this object.
//     * 
//     */
//    public void setEngine(TEngine model) {
//        theEngine = model;
//    }

    /**
     * Returns the magnitude of this ConstantField.
     * @return the magnitude of the field.
     */
    public double getMagnitude() {
        return magnitude;
    }

    /**
     * Sets the magnitude of this ConstantField.
     * 
     * @param newmag the new magnitude of this field.
     */
    /*
     public void setMagnitude(double newmag) {
     PropertyChangeEvent pce =
     PCUtil.makePCEvent(this, "magnitude", magnitude, newmag);
     if (theEngine == null) {
     firePropertyChange(pce);
     magnitude = newmag;
     } else {
     synchronized (theEngine) {
     firePropertyChange(pce);
     magnitude = newmag;
     theEngine.requestSpatial();
     }
     }
     }
     */
    public void setMagnitude(double newmag) {
        Double old = new Double(magnitude);
        magnitude = newmag;
        firePropertyChange("magnitude", old, new Double(magnitude));
        if (theEngine != null) {
            theEngine.requestSpatial();
            if (theEngine.getEngineControl().getSimState() != TEngineControl.RUNNING) {
                magnitude_d = newmag;
            }
        } else {
            magnitude_d = newmag;
        }
    }

    /** 
     * Sets the fieldType of this ConstantField.  Current supported types are B_FIELD and E_FIELD.
     * @param type the new type of field.
     */
    public void setType(int type) {
        fieldType = type;
        if (type == B_FIELD) {
            generatingBField = true;
            generatingEField = false;
        } else if (type == E_FIELD) {
            generatingEField = true;
            generatingBField = false;
        } else {
            TDebug.println(0, "ConstantField type error, setting to B_FIELD!");
            generatingBField = true;
            generatingEField = false;
        }
    }

    /**
     * Returns the electric field generated by this ConstantField at a given position (pos).
     * 
     */
    public Vector3d getE(Vector3d pos) {
        Vector3d efield = new Vector3d();
        efield.scale(magnitude, zdir);
        return efield;
    }

    /**
     * Deprecated, just calles getE(pos).
     */
    public Vector3d getE(Vector3d pos, double t) {
        return getE(pos);
    }

    /**
     * Returns the "flux" value of this ConstantField at a given position.  The flux field is a scalar field whose 
     * isocontours represent fieldlines.
     */
    public double getEFlux(Vector3d pos) {
        double flux = 0;
        Vector3d r = new Vector3d();
        r.sub(pos, position);

        double rlength = r.length();
        double RdotX = r.dot(xdir);

        flux = magnitude * RdotX * RdotX * 100.;
        return flux;
    }

    /** 
     * Returns the electric potential of this ConstantField.  NOTE:  an electric potential has not yet been defined for
     * this object.  Currently returns 0.
     */
    public double getEPotential(Vector3d pos) {
        return 0;
    }

    /**
     * Returns whether this ConstantField is currently generating an electric field (see interface GeneratesE).
     */
    public boolean isGeneratingE() {
        return generatingEField;
    }

    /**
     * Returns the magnetic field generated by this ConstantField at a given position (pos).
     */
    public Vector3d getB(Vector3d pos) {
        Vector3d bfield = new Vector3d();
        bfield.scale(magnitude_d, zdir);
        return bfield;
    }

    /**
     * Deprecated, calls getB(pos).
     */
    public Vector3d getB(Vector3d pos, double t) {
        return getB(pos);
    }

    /**
     * Returns the "flux" value of this ConstantField at a given position.  The flux field is a scalar field whose 
     * isocontours represent fieldlines.
     */
    public double getBFlux(Vector3d pos) {
        double flux = 0;
        Vector3d r = new Vector3d();
        r.sub(pos, position);

        double rlength = r.length();
        double RdotX = r.dot(xdir);

        flux = magnitude_d * RdotX * RdotX * 100. * Math.PI;
        return flux;
    }

    /** 
     * Returns the magnetic potential of this ConstantField.  NOTE:  a magnetic potential has not yet been defined for
     * this object.  Currently returns 0.
     */
    public double getBPotential(Vector3d pos) {
        return 0;
    }

    /**
     * Returns whether this ConstantField is currently generating a magnetic field (see interface GeneratesB).
     */
    public boolean isGeneratingB() {
        return generatingBField;
    }

    public int getNumberDependentValues() {
        return super.getNumberDependentValues() + (isIntegrating ? 1 : 0);
    }

    public void getDependentValues(double[] depValues, int offset) {
        if (!isIntegrating) {
            magnitude_d = magnitude;
            super.getDependentValues(depValues, offset);
            return;
        }

        super.getDependentValues(depValues, offset);
        offset += super.getNumberDependentValues();

        depValues[offset++] = magnitude_d;
        magnitude_derivative = (magnitude - magnitude_d) / theEngine.getDeltaTime();
    }

    public void setDependentValues(double[] newValues, int offset) {
        if (!isIntegrating) {
            super.setDependentValues(newValues, offset);
            return;
        }
        super.setDependentValues(newValues, offset);
        offset += super.getNumberDependentValues();
        magnitude_d = newValues[offset++];
    }

    public void getDependentDerivatives(double[] depDerivatvies, int offset, double time) {
        if (!isIntegrating) {
            super.getDependentDerivatives(depDerivatvies, offset, time);
            return;
        }
        // Super's dependent derivatives.
        super.getDependentDerivatives(depDerivatvies, offset, time);
        offset += super.getNumberDependentValues();
        // Add magnitude derivative.
        depDerivatvies[offset++] = magnitude_derivative;
    }

    /**
     * @return Returns the isIntegrating.
     */
    public boolean isIntegrating() {
        return isIntegrating;
    }

    /**
     * @param isIntegrating The isIntegrating to set.
     */
    public void setIntegrating(boolean isIntegrating) {
        this.isIntegrating = isIntegrating;
    }
}