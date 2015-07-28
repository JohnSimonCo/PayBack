package com.johnsimon.payback.util;

public class ReadResult<DataType, ErrorType> {
	public DataType data;
	public ErrorType error = null;

	public static <D, E> ReadResult<D, E> success(D data) {
		return new ReadResult<>(data, null);
	}
	public static <D, E> ReadResult<D, E>  error(E error) {
		return new ReadResult<>(null, error);
	}

	private ReadResult(DataType data, ErrorType error) {
		this.data = data;
		this.error = error;
	}

	public boolean isSuccess() {
		return error == null;
	}
}