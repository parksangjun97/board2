package com.example.board2.service;

import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.board2.dao.BoardDao;
import com.example.board2.dto.Board;
@Service
public class BoardServiceImpl implements BoardService{

	
	@Autowired	//boardMapper에 있는 sql문을 BoardServiceImpl로 읽어와서 의존성주입하여 객체생성 한것.
	private BoardDao boardMapper; // BoardMapper의 의존성 주입
	
	@Override
	public int maxNum() throws Exception {
		return boardMapper.maxNum();
	}

	@Override
	public void insertData(Board board) throws Exception {
		boardMapper.insertData(board);
	}

	@Override
	public int getDataCount(String searchKey, String searchValue) throws Exception {
		return boardMapper.getDataCount(searchKey, searchValue);
	}

	@Override
	public List<Board> getLists(int start, int end, String searchKey, String searchValue) throws Exception {
		return boardMapper.getLists(start, end, searchKey, searchValue);
	}

	@Override
	public Board getReadData(int num) throws Exception {
		return boardMapper.getReadData(num);
	}

	@Override
	public void updateHitCount(int num) throws Exception {
		boardMapper.updateHitCount(num);
	}

	@Override
	public void updateData(Board board) throws Exception {
		boardMapper.updateData(board);
	}

	@Override
	public void deleteData(int num) throws Exception {
		boardMapper.deleteData(num);
		
	}
}
