package net.microtrash.slicecam.data;

import java.util.ArrayList;

public class Composition {
	private String objectId;
	private ArrayList<Slice> slices;
	
	
	public ArrayList<Slice> getSlices() {
		if(slices == null){
			slices = new ArrayList<Slice>();
		}
		return slices;
	}
	public void setSlices(ArrayList<Slice> slices) {
		this.slices = slices;
	}
}
