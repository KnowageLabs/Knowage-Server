package it.eng.spagobi.commons.robobraillerconverter.jobmanager;

public enum JobState {
	
	
	ERROR("0"),INPROGRESS("2"),DONE("1");
	private String state;
	
	private JobState(String state) {
        this.state = state;
    }

	 public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public static JobState getEnumNamebyState(String state){
		 JobState[] values = JobState.values();
		 
		 for(int i=0;i<values.length;i++){
			 if(values[i].state.equals(state)){
				 return values[i];
			 };
		 }
	       return null;
	 }
}
