
import java.util.ArrayList;
import java.util.List;

public class Parameter {

    static ArrayList<Parameter> copyParameters(List<Parameter> parameters) {
        ArrayList<Parameter> t = new ArrayList<>();
        parameters.forEach((parameter) -> {
            t.add(parameter);
        });
        return t;
    }
    String parameterName;
    String type;
    List<Object> values;
    Object currentValue;

    public Parameter(String p, String t, List<Object> v) {
        parameterName = p;
        type = t;
        values = v;
        currentValue = v.get(0);
    }

    public Parameter(Parameter p) {
        parameterName = p.getName();
        type = p.getType();
        values = new ArrayList<>();
        for (Object o : values) {
            values.add(o);
        }
        currentValue = p.getValue();
    }

    public Parameter(String p, String t, Double min, Double max, Double step) {
        parameterName = p;
        type = t;

        List<Object> v = new ArrayList<>();

        Double lastValue = min;
        while (lastValue < max) {
            v.add(lastValue);
            lastValue = lastValue + step;
        }
        currentValue = min;
        values = v;
    }

    public Parameter(String p, String t, Integer min, Integer max, Integer step) {
        parameterName = p;
        type = t;

        List<Object> v = new ArrayList<>();

        Integer lastValue = min;
        while (lastValue < max) {
            v.add(lastValue);
            lastValue = lastValue + step;
        }
        currentValue = min;
        values = v;
    }

    public String getName() {
        return parameterName;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return currentValue;
    }

    public void setNextValue() {
        int index = values.indexOf(currentValue);
        currentValue = values.get((index + 1) % values.size());
    }

    public void setPreviousValue() {
        int index = values.indexOf(currentValue);
        if (index == 0) {
            currentValue = values.get(values.size() - 1);
        } else {
            currentValue = values.get(index - 1);
        }
    }

    public void setRandomValue() {
        currentValue = values.stream().findAny();
    }

    public int nbValues() {
        return values.size();
    }

    @Override
    public String toString() {
        return (parameterName + " = " + currentValue);
    }
}
