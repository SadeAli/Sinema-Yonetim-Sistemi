public class FilterCondition {
    private String fieldName;
    private Object value;
    private Relation relation;

    public FilterCondition(String fieldName, Object value, Relation relation) {
        this.fieldName = fieldName;
        this.value = value;
        this.relation = relation;
    }

    // Getters

	public String getFieldName() {
		return fieldName;
	}

	public Object getValue() {
		return value;
	}

	public Relation getRelation() {
		return relation;
	}

	public String getRelationOperator() {
		switch (relation) {
			case EQUALS:
				return "=";
			case NOT_EQUALS:
				return "!=";
			case GREATER_THAN:
				return ">";
			case LESS_THAN:
				return "<";
			case GREATER_THAN_OR_EQUALS:
				return ">=";
			case LESS_THAN_OR_EQUALS:
				return "<=";
			default:
				return "";
		}
	}

    public enum Relation {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
		GREATER_THAN_OR_EQUALS,
		LESS_THAN_OR_EQUALS
    }
}
