package net.microtrash.slicecam.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microtrash.slicecam.Static;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class Composition {
	private ArrayList<ParseObject> slices;
	private ParseObject parseObject;

	public String getParseObjectId() {
		if (parseObject != null) {
			return parseObject.getObjectId();
		}
		return null;
	}

	public ArrayList<ParseObject> getSlices() {
		if (slices == null) {
			slices = new ArrayList<ParseObject>();
		}
		return slices;
	}

	public void setSlices(ArrayList<ParseObject> slices2) {
		this.slices = slices2;
	}

	public void setSlices(List<ParseObject> slices2) {
		getSlices().clear();
		for (ParseObject slice : slices2) {
			slices.add(slice);
		}
	}

	public ParseObject getLastSlice() {
		if (getSlices().size() != 0) {
			ParseObject lastSlice = slices.get(slices.size() - 1);
			return lastSlice;
		}
		return null;
	}

	public ParseObject getParseObject() {
		return parseObject;
	}

	public void setParseObject(ParseObject parseObject) {
		this.parseObject = parseObject;
	}

	public ParseUser getLastUser() {
		if (getLastSlice() != null) {
			return getLastSlice().getParseUser(Static.FIELD_CREATED_BY);
		}
		return null;
	}

	public ArrayList<String> getSlicesFilenames() {

		ArrayList<String> filenames = new ArrayList<String>();
		for (ParseObject slice : getSlices()) {
			String sliceFilename = Static.createSliceFilename(getParseObjectId(),
					slice.getInt(Static.FIELD_STEP));
			filenames.add(sliceFilename + "." + Static.IMAGE_FILE_EXTENSION);
		}
		Collections.sort(filenames);
		return filenames;
	}

	public ArrayList<String> getSlicesFilenpaths() {
		ArrayList<String> filepaths = new ArrayList<String>();
		for (ParseObject slice : getSlices()) {
			filepaths.add(Static.getFullSliceFilepath(slice));
		}
		Collections.sort(filepaths);
		return filepaths;
	}
}
