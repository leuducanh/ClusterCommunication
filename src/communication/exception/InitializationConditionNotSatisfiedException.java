package communication.exception;

public class InitializationConditionNotSatisfiedException extends Exception{

	public String reason;
	
	public InitializationConditionNotSatisfiedException(String reason) {
		super(String.format("This condition did not satisfied: %s, so the process can not run", reason));
	}	
}
