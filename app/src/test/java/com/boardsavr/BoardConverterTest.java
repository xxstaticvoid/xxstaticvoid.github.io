package com.boardsavr;

import com.boardsaver.BoardConverter;

import org.junit.Test;



public class BoardConverterTest {

    private BoardConverter converter = new BoardConverter();

    @Test
    public void testConvertToFen() {
        String state = converter.convertToFen(BoardConverter.startingBoardState);
        assert state.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

    }
}
