package com.mycompany.prac3;


import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
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
    
    public Prac3 () {
        this.JENAPath = "./";
        this.OntologyFile = "test_Practica.owl";
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
        
        // crear nodos + adjacencia entre nodos
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
        
        Property alma_esta_en = model.createDatatypeProperty(BASE_URI + "#Almacenamiento_esta_en");
        Property reco_esta_en = model.createDatatypeProperty(BASE_URI + "#Recolector_esta_en");
        
        // crear agente almacenamiento
        OntClass agenteAlmacenamiento = model.getOntClass(BASE_URI + "#Almacenamiento");
        Individual almacenamientoInstance = agenteAlmacenamiento.createIndividual(BASE_URI + "#Almacenador1");
        Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + nodes[1] + "Instance");
        almacenamientoInstance.addProperty(alma_esta_en, nodeInstance);
        
        // crear agente recolector
        OntClass agenteRecolector = model.getOntClass(BASE_URI + "#Recolector");
        Individual recolectorInstance = agenteRecolector.createIndividual(BASE_URI + "#Recolector1");
        nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + nodes[4] + "Instance");
        recolectorInstance.addProperty(reco_esta_en, nodeInstance);
        
        Belief ontology = new TransientBelief("ontology", model);
        Capability c = getCapability();
        BeliefBase bb = c.getBeliefBase();
        
        bb.addBelief(ontology);
        
        Plan descargar = new DefaultPlan(DescargarGoal.class, DescargarPlan.class);
        c.getPlanLibrary().addPlan(descargar);
        
        this.addGoal(new DescargarGoal());
    }

    public class DescargarGoal implements Goal {
        private OntModel model;

        private boolean testEquivalentClass(String checkEquiv) {
            Individual instance = model.getIndividual(BASE_URI + "#Recolector1");
            boolean ex = instance.hasOntClass(BASE_URI + "#" + checkEquiv);
            return ex;
        }
        
        private Individual checkAdjacent(String a) {
            Individual instance = model.getIndividual(BASE_URI + "#Recolector1");
            Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
            RDFNode adjacentNodes = instance.getPropertyValue(nameProperty);
            System.out.println(adjacentNodes.toString());
            return instance;
        }
        
        public void setModel(OntModel model) { this.model = model; }
        
        public Individual check_descarga() {
            String[] nodes = {"OneAwayAlmacenamiento", "TwoAwayAlmacenamiento", 
                                "ThreeAwayAlmacenamiento", "FourAwayAlmacenamiento", 
                                "FiveAwayAlmacenamiento"};
            for (int i = 0; i < nodes.length; i++){
                String n = nodes[i];
                if (testEquivalentClass(n)) {
                    System.out.println(n);
                    if (i == 0) return model.getIndividual(BASE_URI + "#Recolector1");
                    return checkAdjacent(nodes[i-1]);
                }
            }
            return null;
        }
    }
    
    public class DescargarPlan extends AbstractPlanBody {
        @Override
        public void action() {
            BeliefBase bb = getBeliefBase();
            OntModel model = (OntModel) (bb.getBelief("ontology").getValue());
                    
            DescargarGoal g = (DescargarGoal) getGoal();
            g.setModel(model);
            g.check_descarga();
            /*if (g.check_descarga()) setEndState(Plan.EndState.SUCCESSFUL);
            else {
                Individual nextNode = g.getClosestNode();
            }*/
        }
    }
}
