///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010, 2014, 2015 by Peter Spirtes, Richard Scheines, Joseph   //
// Ramsey, and Clark Glymour.                                                //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.KnowledgeBoxInput;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradSerializableUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 */
public class BayesPmWrapper implements SessionModel, GraphSource, KnowledgeBoxInput {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * The wrapped BayesPm.
     *
     * @serial Cannot be null.
     */
    private BayesPm bayesPm;

    //==============================CONSTRUCTORS=========================//

    /**
     * Creates a new BayesPm from the given DAG and uses it to construct a new
     * BayesPm.
     */
    public BayesPmWrapper(Dag graph, Parameters params) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        int lowerBound, upperBound;

        if (params.getString("initializationMode", "manualRetain").equals("manual")) {
            lowerBound = upperBound = 2;
        }
        else if (params.getString("initializationMode", "manualRetain").equals("automatic")) {
            lowerBound = params.getInt("minCategories", 2);
            upperBound = params.getInt("maxCategories", 2);
        }
        else {
            throw new IllegalStateException("Unrecognized type.");
        }

        this.bayesPm = new BayesPm(graph, lowerBound, upperBound);
        log(bayesPm);
    }

    public BayesPmWrapper(Dag graph, BayesPm bayesPm, Parameters params) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        if (bayesPm == null) {
            throw new NullPointerException("BayesPm must not be null");
        }

        int lowerBound, upperBound;

        if (params.getString("initializationMode", "manualRetain").equals("manual")) {
            lowerBound = upperBound = 2;
            this.bayesPm = new BayesPm(graph, bayesPm, lowerBound, upperBound);
        }
        else if (params.getString("initializationMode", "manualRetain").equals("automatic")) {
            lowerBound = params.getInt("minCategories", 2);
            upperBound = params.getInt("maxCategories", 2);
            this.bayesPm = new BayesPm(graph, lowerBound, upperBound);
        }
        else {
            throw new IllegalStateException("Unrecognized type.");
        }
        log(bayesPm);
    }

    /**
     * Creates a new BayesPm from the given workbench and uses it to construct a
     * new BayesPm.
     *
     * @throws RuntimeException If the parent graph cannot be converted into a
     *                          DAG.
     */
    public BayesPmWrapper(GraphWrapper graphWrapper, Parameters params) {
        if (graphWrapper == null) {
            throw new NullPointerException("Graph must not be null.");
        }

//        if (graphWrapper.getGraph().getNodes().isEmpty()) {
//            throw new IllegalArgumentException("The parent graph is empty.");
//        }

        Dag graph;

        try {
            graph = new Dag(graphWrapper.getGraph());
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "The parent graph cannot be converted to " + "a DAG.");
        }

        int lowerBound, upperBound;

        if (params.getString("bayesPmInitializationMode", "automatic").equals("manual")) {
            lowerBound = upperBound = 2;
        }
        else if (params.getString("bayesPmInitializationMode", "automatic").equals("automatic")) {
            lowerBound = params.getInt("minCategories", 2);
            upperBound = params.getInt("maxCategories", 2);
        }
        else {
            throw new IllegalStateException("Unrecognized type.");
        }

        this.bayesPm = new BayesPm(graph, lowerBound, upperBound);
        log(bayesPm);
    }

    public BayesPmWrapper(GraphWrapper graphWrapper,
            BayesPmWrapper oldBayesPmWrapper, Parameters params) {
        try {
            if (graphWrapper == null) {
                throw new NullPointerException("Graph must not be null.");
            }

            if (oldBayesPmWrapper == null) {
                throw new NullPointerException("BayesPm must not be null");
            }

            Dag graph = new Dag(graphWrapper.getGraph());

            int lowerBound, upperBound;

            if (params.getString("initializationMode", "manualRetain").equals("manual")) {
                lowerBound = upperBound = 2;
                this.bayesPm = new BayesPm(graph,
                        oldBayesPmWrapper.getBayesPm(), lowerBound, upperBound);
            }
            else
            if (params.getString("initializationMode", "manualRetain").equals("automatic")) {
                lowerBound = params.getInt("lowerBoundNumVals", 2);
                upperBound = params.getInt("upperBoundNumVals", 2);
                this.bayesPm = new BayesPm(graph, lowerBound, upperBound);
            }
            else {
                throw new IllegalStateException("Unrecognized type.");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "The parent graph cannot be converted to " + "a DAG.");
        }
        log(bayesPm);
    }

    public BayesPmWrapper(BayesEstimatorWrapper wrapper) {
        this.bayesPm = new BayesPm(wrapper.getEstimatedBayesIm().getBayesPm());
        log(bayesPm);
    }

    public BayesPmWrapper(BayesImWrapper wrapper) {
        this.bayesPm = new BayesPm(wrapper.getBayesIm().getBayesPm());
        log(bayesPm);
    }

    public BayesPmWrapper(GraphSource graphWrapper, DataWrapper dataWrapper) {
        this(new Dag(graphWrapper.getGraph()), dataWrapper);
    }

    public BayesPmWrapper(Dag graph, DataWrapper dataWrapper) {
        DataSet dataSet =
                (DataSet) dataWrapper.getSelectedDataModel();
        List vars = dataSet.getVariables();

        Map<String, DiscreteVariable> nodesToVars =
                new HashMap<>();
        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            DiscreteVariable var = (DiscreteVariable) vars.get(i);
            String name = var.getName();
            Node node = new GraphNode(name);
            nodesToVars.put(node.getName(), var);
        }

        BayesPm bayesPm = new BayesPm(graph);
        List<Node> nodes = bayesPm.getDag().getNodes();

        for (Node node : nodes) {
            Node var = nodesToVars.get(node.getName());

            if (var instanceof DiscreteVariable) {
                DiscreteVariable var2 = nodesToVars.get(node.getName());
                int numCategories = var2.getNumCategories();
                List<String> categories = new ArrayList<>();
                for (int j = 0; j < numCategories; j++) {
                    categories.add(var2.getCategory(j));
                }
                bayesPm.setCategories(node, categories);
            }
        }

        this.bayesPm = bayesPm;
        log(bayesPm);
    }

    public BayesPmWrapper(GraphWrapper graphWrapper,
            Simulation simulation) {
        this(graphWrapper, (DataWrapper) simulation);
    }

    public BayesPmWrapper(AlgorithmRunner wrapper, Parameters params) {
        this(new Dag(wrapper.getResultGraph()), params);
    }

    public BayesPmWrapper(AlgorithmRunner wrapper, DataWrapper dataWrapper) {
        this(new Dag(wrapper.getResultGraph()), dataWrapper);
    }

    public BayesPmWrapper(AlgorithmRunner wrapper, Simulation simulation) {
        this(new Dag(wrapper.getResultGraph()), simulation);
    }

    public BayesPmWrapper(BayesEstimatorWrapper wrapper, Simulation simulation) {
        this(new Dag(wrapper.getGraph()), simulation);
    }

    public BayesPmWrapper(BayesEstimatorWrapper wrapper,
                          DataWrapper dataWrapper) {
        this(new Dag(wrapper.getGraph()), dataWrapper);
    }

    /**
     * Creates a new BayesPm from the given workbench and uses it to construct a
     * new BayesPm.
     *
     * @throws RuntimeException If the parent graph cannot be converted into a
     *                          DAG.
     */
    public BayesPmWrapper(DagWrapper dagWrapper, Parameters params) {
        if (dagWrapper == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        Dag graph;

        try {
            graph = new Dag(dagWrapper.getDag());
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "The parent graph cannot be converted to " + "a DAG.");
        }

        int lowerBound, upperBound;

        if (params.getString("initializationMode", "manualRetain").equals("manual")) {
            lowerBound = upperBound = 2;
        }
        else if (params.getString("initializationMode", "manualRetain").equals("automatic")) {
            lowerBound = params.getInt("lowerBoundNumVals", 2);
            upperBound = params.getInt("upperBoundNumVals", 2);
        }
        else {
            throw new IllegalStateException("Unrecognized type.");
        }

        this.bayesPm = new BayesPm(graph, lowerBound, upperBound);
        log(bayesPm);
    }

    public BayesPmWrapper(DagWrapper dagWrapper,
            BayesPmWrapper oldBayesPmWrapper, Parameters params) {
        try {
            if (dagWrapper == null) {
                throw new NullPointerException("Graph must not be null.");
            }

            if (oldBayesPmWrapper == null) {
                throw new NullPointerException("BayesPm must not be null");
            }

            Dag graph = new Dag(dagWrapper.getDag());

            int lowerBound, upperBound;

            if (params.getString("initializationMode", "manualRetain").equals("manual")) {
                lowerBound = upperBound = 2;
                this.bayesPm = new BayesPm(graph,
                        oldBayesPmWrapper.getBayesPm(), lowerBound, upperBound);
            }
            else
            if (params.getString("initializationMode", "manualRetain").equals("automatic")) {
                lowerBound = params.getInt("lowerBoundNumVals", 2);
                upperBound = params.getInt("upperBoundNumVals", 2);
                this.bayesPm = new BayesPm(graph, lowerBound, upperBound);
            }
            else {
                throw new IllegalStateException("Unrecognized type.");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "The parent graph cannot be converted to " + "a DAG.");
        }
        log(bayesPm);
    }

    public BayesPmWrapper(DagWrapper dagWrapper, DataWrapper dataWrapper) {
        DataSet dataSet =
                (DataSet) dataWrapper.getSelectedDataModel();
        List vars = dataSet.getVariables();
        Map<String, DiscreteVariable> nodesToVars =
                new HashMap<>();
        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            DiscreteVariable var = (DiscreteVariable) vars.get(i);
            String name = var.getName();
            Node node = new GraphNode(name);
            nodesToVars.put(node.getName(), var);
        }

        Dag graph = new Dag(dagWrapper.getDag());
        BayesPm bayesPm = new BayesPm(graph);
        List<Node> nodes = bayesPm.getDag().getNodes();

        for (Node node : nodes) {
            Node var = nodesToVars.get(node.getName());

            if (var instanceof DiscreteVariable) {
                DiscreteVariable var2 = nodesToVars.get(node.getName());
                int numCategories = var2.getNumCategories();
                List<String> categories = new ArrayList<>();
                for (int j = 0; j < numCategories; j++) {
                    categories.add(var2.getCategory(j));
                }
                bayesPm.setCategories(node, categories);
            }
        }

        this.bayesPm = bayesPm;
        log(bayesPm);                
    }

    public BayesPmWrapper(DagWrapper dagWrapper, Simulation dataWrapper) {
        this(dagWrapper, (DataWrapper) dataWrapper);
    }

    public BayesPmWrapper(Simulation simulation) {
        List<Graph> graphs = simulation.getGraphs();

        if (!(graphs.size() == 1)) {
            throw new IllegalArgumentException("Simulation must contain exactly one graph/data pair.");
        }

        this.bayesPm = new BayesPm(graphs.get(0));
        log(bayesPm);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see TetradSerializableUtils
     */
    public static BayesPmWrapper serializableInstance() {
        return new BayesPmWrapper(Dag.serializableInstance(), new Parameters());
    }

    //=============================PUBLIC METHODS========================//

    public BayesPm getBayesPm() {
        return this.bayesPm;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (bayesPm == null) {
            throw new NullPointerException();
        }
    }

    public Graph getGraph() {
        return bayesPm.getDag();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //================================= Private Methods ==================================//

    private void log(BayesPm pm){
        TetradLogger.getInstance().log("info", "Bayes Parametric Model (Bayes PM)");
        TetradLogger.getInstance().log("pm", pm.toString());

    }

	public Graph getSourceGraph() {
		return getGraph();
	}

    public Graph getResultGraph() {
        return getGraph();
    }

    public List<String> getVariableNames() {
		return getGraph().getNodeNames();
	}

	public List<Node> getVariables() {
		return getGraph().getNodes();
	}


}







