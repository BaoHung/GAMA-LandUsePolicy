
import java.util.ArrayList;
import java.util.List;

public abstract class ExperimentPlan {

    List<Parameter> parameters;

    String sourcePath;
    int finalStep;
    String untilCondition;
    String experimentName;
    List<Output> outputs;
    Output minFitOutput;
    private Double computedFitness;

    public Double getComputedFitness() {
        return computedFitness;
    }

    public void setComputedFitness(Double computedFitness) {
        this.computedFitness = computedFitness;
    }

    public ExperimentPlan() {
        parameters = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public void setPath(String path) {
        sourcePath = path;
    }

    public void setFinalStep(int fi) {
        finalStep = fi;
    }

    public void setStopCondition(String stopCondition) {
        untilCondition = stopCondition;
    }

    public void setEperimentName(String expName) {
        experimentName = expName;
    }

    public void addParameter(Parameter p) {
        parameters.add(p);
    }

    public void addOutput(Output o) {
        outputs.add(o);
    }

    public void writeXMLFile(String fileName) {
        XMLWriter writer = new XMLWriter(fileName);
        writer.writeSimulationHeader(sourcePath, finalStep, untilCondition, experimentName);
        writer.writeParameters(parameters);
        writer.writeOutputs(outputs);
        writer.close();
    }

    public abstract List<Parameter> nextParametersSet();

    public abstract boolean hasNextParametersSet();

}
