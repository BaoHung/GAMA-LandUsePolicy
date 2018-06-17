import java.io.FileNotFoundException;

public class TestCallGAMA {

	public static void main(String[] args)  {
		GAMACaller caller = new GAMACaller("LUP/Headless/lup.xml", "LUP/Headless/output");

		caller.runGAMA();
		
		try {
			XMLReader read = new XMLReader("/media/bh/Linux Stuff/Dropbox/USTH/M1/MI2.05 - Complex system/Final Project/Headless/output/simulation-outputs2.xml");
			read.parseXmlFile();
			for( String m : read.getMap().keySet()) {
				System.out.println(" Var "+m);
				read.getMap().get(m).entrySet().stream().forEach(e -> System.out.println("  " + e.getKey() + " - " + e.getValue()));
				System.out.println(read.getFinalValueOf("rate_same_landuse_2010"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
