package com.mycompany.prac3;


import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.ResultBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;

public class Prac3 extends SingleCapabilityAgent {
    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    private static final String BASE_URI = "http://www.semanticweb.org/sid-prac3";
    private static final String MODIFIED_PREFIX = "modified_";
    
    public Prac3(String _JENA_PATH, String _File, String _NamingContext) {
        this.JENAPath = _JENA_PATH;
        this.OntologyFile = _File;
        this.NamingContext = _NamingContext;
    }
    
    public void Pract3 () {
        this.JENAPath = "./";
        this.OntologyFile = "Practica.owl";
        this.NamingContext = "prac3";
    }
    
    public void loadOntology() {
        System.out.println("· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + OntologyFile);
        model.read(NamingContext);
    }

    public void releaseOntology() throws FileNotFoundException {
        System.out.println("· Releasing Ontology");
        if (!model.isClosed()) {
            model.write(new FileOutputStream(JENAPath + File.separator + MODIFIED_PREFIX + OntologyFile, false));
            model.close();
        }
    }

    public void init() {
        loadOntology();
        String[] nodes = {"1", "2", "3", "4", "5"};
        boolean[][] adjacent = {{false, true, true, false, true}, 
                                {true, false, true, false, false}, 
                                {true, true, false, true, false}, 
                                {false, false, true, false, true}, 
                                {true, false, false, true, false}};
        
        int n = nodes.length;
        OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
        for (int i=0; i<n; i++) {
            System.out.println("Adding instance '" + nodes[i] + "'");
            Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + nodes[i] + "Instance");
            
            for (int j=0; j<i; j++) {
                if (adjacent[i][j]) {
                    Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
                    Individual adjacentNode = model.getIndividual(BASE_URI + "#Node" + nodes[j] + "Instance");
                    nodeInstance.addProperty(nameProperty, adjacentNode);
                }
            }
        }
        try {
            releaseOntology();
        } catch(Exception e) {}
    }
}
