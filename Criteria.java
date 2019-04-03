
public class Criteria {
	
	String keyToCheck;
	Filter.ComparisonOperator comparisonOperator;
	String valueToCheck;
	Float valueToCheckAsFloat;
	
	public Criteria(String key, Filter.ComparisonOperator operator, String value){
		keyToCheck = key;
		comparisonOperator = operator;
		valueToCheck = value;
	}
	
	@Override
	public String toString(){
		return keyToCheck + " " + comparisonOperator.GetValue() + " " + valueToCheck;
	}
}
