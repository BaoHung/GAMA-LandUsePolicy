
public class Output {

	public String name;
	public int frameRate;
	public String id;

	public Output(final String name, final int frameRate, final String id) {
		super();
		this.name = name;
		this.frameRate = frameRate;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(final int frameRate) {
		this.frameRate = frameRate;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}
}
