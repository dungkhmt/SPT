package pbts.enums;

public enum ErrorType {
	NO_ERROR,
	DUPLICATE_REQUEST,
	NO_DELIVERY_POINT,
	NO_PICKUP_POINT,
	EXCEED_MAX_STOPS_PEOPLE_REQUEST,
	EXCEED_MAX_DISTANCE_PEOPLE_RQUEST,
	EXCEED_PEOPLE_ON_BOARD,
	DELIVERY_BEFORE_PICKUP,
	EXCEED_TIME_WINDOW
}
