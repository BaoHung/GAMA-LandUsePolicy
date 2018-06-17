
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExhaustivePlan extends ExperimentPlan {

    private static ArrayList<Parameter> copyParameters(List<Parameter> parameters) {
        ArrayList<Parameter> t = new ArrayList<>();
        parameters.forEach((p) -> {
            t.add(p);
        });
        return t;
    }

    int experimentNumber;
    int sizeParameterSpace;

    public ExhaustivePlan() {
        experimentNumber = -1;
        sizeParameterSpace = 1;
    }

    public void addParameter(Parameter p) {
        super.addParameter(p);
        sizeParameterSpace = sizeParameterSpace * p.nbValues();
    }

    public int getExperimentNumber() {
        return experimentNumber;
    }

    @Override
    public List<Parameter> nextParametersSet() {
        experimentNumber++;
        if (experimentNumber != 0) {
            parameters.get(0).setNextValue();
            int previousDomainSpace = 1;
            for (int i = 1; i < parameters.size(); i++) {
                previousDomainSpace = previousDomainSpace * parameters.get(i - 1).nbValues();
                if ((experimentNumber % previousDomainSpace) == 0) {
                    parameters.get(i).setNextValue();
                }
            }
        }
        return parameters;
    }

    @Override
    public boolean hasNextParametersSet() {
        return experimentNumber < sizeParameterSpace - 1;
    }

    public List<Parameter> minimizeFitness() {
        ArrayList<Parameter> params = ExhaustivePlan.copyParameters(parameters);
        Double minValueFitness = Double.MAX_VALUE;
        while (hasNextParametersSet()) {
            System.out.println("--------" + getExperimentNumber() + "-----------");
            List<Parameter> ps = nextParametersSet();
            ps.stream().forEach(p -> System.out.println(p));

            // absolute Path mandatory !!
            String XMLFilepath = "/home/bh/GamaExProject/LUP" + getExperimentNumber();
            writeXMLFile(XMLFilepath + ".xml");

            GAMACaller gama = new GAMACaller(XMLFilepath + ".xml", XMLFilepath);
            gama.runGAMA();

            XMLReader read = null;
            try {
                read = new XMLReader(XMLFilepath + "/simulation-outputs.xml");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ExhaustivePlan.class.getName()).log(Level.SEVERE, null, ex);
            }
            read.parseXmlFile();

            Double valueFitness = Double.parseDouble(read.getFinalValueOf("rate_same_landuse_2010"));
            if (valueFitness < minValueFitness) {
                minValueFitness = valueFitness;

            }

            System.out.println(read.getFinalValueOf("rate_same_landuse_2010"));
        }
        return params;
    }

    public static void main(String[] args) throws FileNotFoundException {
        ExhaustivePlan exp = new ExhaustivePlan();

        exp.setEperimentName("lup_headless");
        exp.setFinalStep(200);
        exp.setPath("/media/bh/Linux Stuff/Dropbox/USTH/M1/MI2.05 - Complex system/Final Project/ProjectUSTH-LUP/models/Model_Complete.gaml");
        exp.setStopCondition("current_date.year = 2010 and current_date.month = 12");

        exp.addParameter(new Parameter("weight_investment", "FLOAT", 0.2, 0.81, 0.3));
        exp.addParameter(new Parameter("weight_cost", "FLOAT", 0.2, 0.81, 0.3));
        exp.addParameter(new Parameter("weight_delay", "FLOAT", 0.2, 0.81, 0.3));

        exp.addOutput(new Output("rate_same_landuse_2010", 1, "1"));

        exp.minimizeFitness()
                .stream()
                .forEach(e -> System.out.println(e.getName()));
        System.out.println("Last fitness: " + exp.getComputedFitness());

    }

    public static void generateXML(String[] args) {
        ExhaustivePlan exp = new ExhaustivePlan();

        exp.setEperimentName("lup_headless");
        exp.setFinalStep(200);
        exp.setPath("/media/bh/Linux Stuff/Dropbox/USTH/M1/MI2.05 - Complex system/Final Project/ProjectUSTH-LUP/models/Model_Complete.gaml");

        exp.setStopCondition("current_date.year = 2010 and current_date.month = 12");

        exp.addParameter(new Parameter("weight_profit", "FLOAT", 0.2, 0.8, 0.1));

        exp.addOutput(new Output("rate_same_landuse_2010", 1, "1"));

        while (exp.hasNextParametersSet()) {
            List<Parameter> ps = exp.nextParametersSet();
            ps.stream().forEach(p -> System.out.println(p));
            exp.writeXMLFile("xml/test" + exp.getExperimentNumber() + ".xml");
        }

    }
}
