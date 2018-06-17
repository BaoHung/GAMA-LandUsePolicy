import java.io.*;
import java.util.List;

public class XMLWriter {

	private BufferedWriter file;

	public XMLWriter(final String f) {
		try {
			this.file = new BufferedWriter(new FileWriter(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public XMLWriter(final BufferedWriter f) {
			this.file = f;
		
	}

	public void close() {
		String res = "</Simulation>";
		try {
			this.file.write(res);
			this.file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public void writeParameters(final List<Parameter> params) {
		StringBuffer sb = new StringBuffer("\t<Parameters>\n");
		
		for(Parameter p : params) {
			sb.append("\t\t<Parameter var=\"").append(p.getName())
						.append("\" type=\"").append(p.getType())
						.append("\" value=\"").append(p.getValue())
						.append("\" />\n");
		}
		
		sb.append("\n\t</Parameters>\n");
		try {
			this.file.write(sb.toString());
			this.file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}

	public void writeOutputs(final List<Output> outputs) {
		StringBuffer sb = new StringBuffer("\t<Outputs>\n");
		
		for(Output o : outputs) {
			sb.append("\t\t<Output id=\"").append(o.getId())
						.append("\" name=\"").append(o.getName())
						.append("\" framerate=\"").append(o.getFrameRate())
						.append("\" />\n");
		}
		
		sb.append("\n\t</Outputs>\n");
		try {
			this.file.write(sb.toString());
			this.file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public void writeSimulationHeader(String path, int step, String untilCondition, String exp) {
		// sourcePath="./predatorPrey/predatorPrey.gaml" finalStep="200" until="length(predator) = 1"  experiment="prey_predatorExp">
		
		String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		res += "<Simulation id=\"\" ";
		res += "sourcePath=\""+path+"\" ";
		res += "finalStep=\""+step+"\" ";
		res += "until=\""+untilCondition+"\" ";
		res += "experiment=\""+exp+"\" ";		
		res += ">\n";
				
		try {
			this.file.write(res);
			this.file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
