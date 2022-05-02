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


public class Prac3 {
    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    private static final String PIZZA_BASE_URI = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
    private static final String MODIFIED_PREFIX = "modified_";

    public Prac3(String _JENA_PATH, String _File, String _NamingContext) {
        this.JENAPath = _JENA_PATH;
        this.OntologyFile = _File;
        this.NamingContext = _NamingContext;
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

    public void getIndividuals() {
        //List of ontology properties
        for (Iterator i = model.listIndividuals().toList().iterator(); i.hasNext(); ) {
            Individual dummy = (Individual) i.next();
            System.out.println("Ontology has individual: ");
            System.out.println("   " + dummy);
            Property nameProperty = model.getProperty(PIZZA_BASE_URI + "#hasPizzaName");
            RDFNode nameValue = dummy.getPropertyValue(nameProperty);
            System.out.println("   hasPizzaName = " + nameValue);
        }
    }

    public void getIndividualsByClass() {
        Iterator<OntClass> classesIt = model.listNamedClasses().toList().iterator();
        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Class: '" + actual.getURI() + "' has individuals:");
            OntClass pizzaClass = model.getOntClass(actual.getURI());
            for (Iterator i = model.listIndividuals(pizzaClass).toList().iterator(); i.hasNext(); ) {
                Individual instance = (Individual) i.next();
                System.out.println("    · " + instance);
            }
        }
    }

    public void getPropertiesByClass() {
        Iterator<OntClass> classesIt = model.listNamedClasses().toList().iterator();
        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Class: '" + actual.getURI() + "' has properties:");
            OntClass pizzaClass = model.getOntClass(actual.getURI());
            //List of ontology properties
            Iterator<OntProperty> itProperties = pizzaClass.listDeclaredProperties().toList().iterator();

            while (itProperties.hasNext()) {
                OntProperty property = itProperties.next();
                System.out.println("    · Name :" + property.getLocalName());
                System.out.println("        · Domain :" + property.getDomain());
                System.out.println("        · Range :" + property.getRange());
                System.out.println("        · Inverse :" + property.hasInverse());
                System.out.println("        · IsData :" + property.isDatatypeProperty());
                System.out.println("        · IsFunctional :" + property.isFunctionalProperty());
                System.out.println("        · IsObject :" + property.isObjectProperty());
                System.out.println("        · IsSymetric :" + property.isSymmetricProperty());
                System.out.println("        · IsTransitive :" + property.isTransitiveProperty());
            }
        }
    }

    private void addInstances(String className) {
        System.out.println("   Adding instance to '" + className + "'");
        OntClass pizzaClass = model.getOntClass(PIZZA_BASE_URI + "#" + className);
        Individual particularPizza = pizzaClass.createIndividual(PIZZA_BASE_URI + "#" + className + "Instance");

        // Data properties (create and use)
        Property nameProperty = model.createDatatypeProperty(PIZZA_BASE_URI + "#hasPizzaName");
        particularPizza.addProperty(nameProperty, "A yummy" + className);

        // Object property (retrieve and use)
        Individual italy = model.getIndividual(PIZZA_BASE_URI + "#Italy");
        Property countryProperty = model.getObjectProperty(PIZZA_BASE_URI + "#hasCountryOfOrigin");
        particularPizza.addProperty(countryProperty, italy);
    }

    public void getClasses() {
        //List of ontology classes
        Iterator<OntClass> classesIt = model.listNamedClasses().toList().iterator();
        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Ontology has class: " + actual.getURI());
        }
    }

    public void runSparqlQueryDataProperty() {
        String queryString =
                "  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                        + "PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> "
                        + "SELECT ?Pizza ?PizzaName "
                        + "WHERE {?Pizza a ?y. "
                        + "		  ?y rdfs:subClassOf pizza:Pizza. "
                        + "		  ?Pizza pizza:hasPizzaName ?PizzaName}";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        for (Iterator iter = results; iter.hasNext(); ) {
            ResultBinding res = (ResultBinding) iter.next();
            Object Pizza = res.get("Pizza");
            Object PizzaName = res.get("PizzaName");
            System.out.println("Pizza = " + Pizza + " <-> " + PizzaName);
        }
        qe.close();
    }

    public void runSparqlQueryObjectProperty() {
        String queryStringMatch = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> SELECT ?Pizza ?PizzaBase ?PizzaTopping where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. ?Pizza pizza:hasBase ?PizzaBase. ?Pizza pizza:hasTopping ?PizzaTopping. ?Pizza pizza:hasPizzaName \"MySuperMarioPizza\"}";
        String queryStringRegExp = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> SELECT ?Pizza ?PizzaBase ?PizzaTopping where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. ?Pizza pizza:hasBase ?PizzaBase. ?Pizza pizza:hasTopping ?PizzaTopping. ?Pizza pizza:hasPizzaName?PizzaName. FILTER regex(?PizzaName, \"^My\") }";
        String queryStringInt = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> SELECT ?Pizza ?PizzaBase ?PizzaTopping where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. ?Pizza pizza:hasBase ?PizzaBase. ?Pizza pizza:hasTopping ?PizzaTopping. ?Pizza pizza:hasPrice ?PizzaPrice. FILTER (?PizzaPrice < 20) }";

        Query query = QueryFactory.create(queryStringInt);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        for (Iterator iter = results; iter.hasNext(); ) {
            ResultBinding res = (ResultBinding) iter.next();
            Object Pizza = res.get("Pizza");
            Object PizzaBase = res.get("PizzaBase");
            Object PizzaTopping = res.get("PizzaTopping");
            System.out.println("Pizza = " + Pizza + " <-> " + PizzaBase + " <-> " + PizzaTopping);
        }
        qe.close();
    }

    public void runSparqlQueryModify() {
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> " +
                "SELECT ?Pizza ?eaten where {" +
                "?Pizza a ?y. " +
                "?y rdfs:subClassOf pizza:Pizza. " +
                "Optional {?Pizza pizza:eaten ?eaten}}";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        for (Iterator iter = results; iter.hasNext(); ) {
            ResultBinding res = (ResultBinding) iter.next();
            Object Pizza = res.get("Pizza");
            Object eaten = res.get("eaten");
            if (eaten == null) {
                System.out.println("Pizza = " + Pizza + " <-> false");
                Individual actualPizza = model.getIndividual(Pizza.toString());
                Property eatenProperty = model.createDatatypeProperty(PIZZA_BASE_URI + "#eaten");
                Literal rdfBoolean = model.createTypedLiteral(Boolean.valueOf("true"));
                actualPizza.addProperty(eatenProperty, rdfBoolean);
            } else {
                System.out.println("Pizza = " + Pizza + " <-> " + eaten);
            }
        }
        qe.close();
    }

    private void testEquivalentClass() {
        System.out.println("· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + File.separator + MODIFIED_PREFIX + OntologyFile);
        model.read(NamingContext);
        Individual instance = model.getIndividual(PIZZA_BASE_URI + "#FourSeasonsInstance");
        boolean isRealItalian = instance.hasOntClass(PIZZA_BASE_URI + "#RealItalianPizza");
        boolean isSpicy = instance.hasOntClass(PIZZA_BASE_URI + "#SpicyPizza");
        System.out.println("FourSeasonsInstance classifies as RealItalianPizza?: " + isRealItalian);
        System.out.println("FourSeasonsInstance classifies as SpicyPizza?: " + isSpicy);
        model.close();
    }
    
    private void myChanges() {
        // Add cheese, base, meat topping
        OntClass cheeseTopping = model.getOntClass(PIZZA_BASE_URI + "#" + "CheeseTopping");
        OntClass sistemas = model.createClass(PIZZA_BASE_URI + '#' + "SistemasCheeseTopping");
        sistemas.addSuperClass(cheeseTopping);
        
        OntClass pizzaBase = model.getOntClass(PIZZA_BASE_URI + "#" + "PizzaBase");
        OntClass inteligente = model.createClass(PIZZA_BASE_URI + '#' + "InteligentePizzaBase");
        inteligente.addSuperClass(pizzaBase);
        
        OntClass meatTopping = model.getOntClass(PIZZA_BASE_URI + "#" + "MeatTopping");
        OntClass distribuidos = model.createClass(PIZZA_BASE_URI + '#' + "DistribuidosMeatTopping");
        distribuidos.addSuperClass(meatTopping);
        
        System.out.println("   Adding instance to 'SID" + "'");
        OntClass pizzaClass = model.getOntClass(PIZZA_BASE_URI + "#Pizza");
        Individual pizzasSID = pizzaClass.createIndividual(PIZZA_BASE_URI + "#SID");
        Individual sistemas1 = sistemas.createIndividual(PIZZA_BASE_URI + "sistemas1");
        Individual inteligentes1 = sistemas.createIndividual(PIZZA_BASE_URI + "inteligentes1");
        Individual distribuidos1 = sistemas.createIndividual(PIZZA_BASE_URI + "distribuidos1");
        
        // Add SID inatance
        Property toppingProperty = model.createDatatypeProperty(PIZZA_BASE_URI + "#hasTopping");
        Property baseProperty = model.createDatatypeProperty(PIZZA_BASE_URI + "#hasBase");
        
        Property nameProperty = model.createDatatypeProperty(PIZZA_BASE_URI + "#hasPizzaName");
        pizzasSID.addProperty(nameProperty, "A yummy SID");
        
        pizzasSID.addProperty(baseProperty, sistemas1);
        pizzasSID.addProperty(toppingProperty, inteligentes1);
        pizzasSID.addProperty(toppingProperty, distribuidos1);
        
    }
    
    private void checkCostumization() {
        //test
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + File.separator + MODIFIED_PREFIX + OntologyFile);
        model.read(NamingContext);
        Individual instance = model.getIndividual(PIZZA_BASE_URI + "#FourSeasonsInstance");
        for (OntClass it : instance.listOntClasses(false).toList()) {
            if (it.getURI() != null)
                System.out.println(it.getURI());
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        System.out.println("Starting tester...");
        String path = "./";
        String owlFile = "OntologiaPizza.owl";
        String namespace = "pizza";
        Prac3 tester = new Prac3(path, owlFile, namespace);
        
        System.out.println("---------------\n\nLoading ontology");
        tester.loadOntology();
        System.out.println("---------------\n\nGet classes");
        tester.getClasses();
        System.out.println("---------------\n\nGet properties");
        tester.getPropertiesByClass();
        System.out.println("---------------\n\nGet all individuals");
        tester.getIndividuals();
        System.out.println("---------------\n\nGroup individuals by class");
        tester.getIndividualsByClass();
        System.out.println("---------------\n\nRun a SPARQL query about a data property");
        tester.runSparqlQueryDataProperty();
        System.out.println("---------------\n\nRun a SPARQL query about an object property");
        tester.runSparqlQueryObjectProperty();
        System.out.println("---------------\n\nRun a modification using the JENA Ontology API");
        tester.addInstances("FourSeasons");
        System.out.println("---------------\n\nRun a modification via SPARQL");
        tester.runSparqlQueryModify();
        System.out.println("---------------\n\nRe-run the modification via SPARQL");
        tester.runSparqlQueryModify();
        System.out.println("---------------\n\nRunning my modifications");
        tester.myChanges();
        System.out.println("---------------\n\nRelease and save ontology");
        tester.releaseOntology();
        System.out.println("---------------\n\nCheck equivalent class inference");
        tester.checkCostumization();
        System.out.println("---------------\n\nCheck equivalent class inference");
        tester.testEquivalentClass();
    }
}
