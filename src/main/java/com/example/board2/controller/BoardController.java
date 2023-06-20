package com.example.board2.controller;
import com.example.board2.dto.Board;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.board2.util.MyUtil;
import com.example.board2.service.BoardService;

import jakarta.servlet.http.HttpServletRequest;
@Controller
public class BoardController {
	@Autowired
	private BoardService boardService; //얘를 호출하면 BoardServiceImpl이 딸려들어옴

	@Autowired
	MyUtil myUtil; //@Service로 구현된 MyUtil을 불러온것
	
	@RequestMapping(value = "/")
	public String index() throws Exception{
		return "/index";
	}
	
	@RequestMapping(value = "/created", method = RequestMethod.GET)
	public String created() throws Exception{
		return "bbs/created";
	}
	
	@RequestMapping(value = "/created", method = RequestMethod.POST)
	public String createdOK(Board board, HttpServletRequest request, Model model) {
		try {
			int maxNum = boardService.maxNum();
			
			board.setNum(maxNum + 1);
			board.setIpAddr(request.getRemoteAddr());
			
			boardService.insertData(board);			
		} catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "게시글 작성 중 에러가 발생했습니다.");
			return "bbs/created";
		}
		
		return "redirect:/list";
		
	}
	
	@RequestMapping(value = "/list", 
			method = {RequestMethod.GET,RequestMethod.POST})
	public String list(Board board, HttpServletRequest request, Model model){
		
		try {
			String pageNum = request.getParameter("pageNum");
			
			int currentPage = 1;
			
			if(pageNum!=null)
				currentPage = Integer.parseInt(pageNum);
			
			String searchKey = request.getParameter("searchKey");
			String searchValue = request.getParameter("searchValue");
			
			if(searchValue==null) {
				searchKey = "subject";
				searchValue = "";
			}else {
				if(request.getMethod().equalsIgnoreCase("GET")) {
					searchValue = URLDecoder.decode(searchValue, "UTF-8");
				}
			}
			
			int dataCount = boardService.getDataCount(searchKey, searchValue);
			
			int numPerPage = 5;
			int totalPage = myUtil.getPageCount(numPerPage, dataCount);
			
			if(currentPage>totalPage)
				currentPage = totalPage;
			
			int start = (currentPage-1)*numPerPage+1; // 1 6 11 16
			int end = currentPage*numPerPage;
			
			List<Board> lists = boardService.getLists(start, end, searchKey, searchValue);
			
			String param = "";
			
			if(searchValue!=null&&!searchValue.equals("")) { //널을 찾아내지 못하는경우가 있기때문에 양쪽에 부정문을 써준다.
				param = "searchKey=" + searchKey;
				param+= "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
			}
			
			String listUrl = "/list";
			
			if(!param.equals("")) {
				listUrl += "?" + param;
			}
			
			String pageIndexList = myUtil.pageIndexList(currentPage, totalPage, listUrl);
			
			String articleUrl = "/article?pageNum=" + currentPage;
			
			if(!param.equals("")) {
				articleUrl += "&" + param;
			}
			
			
			model.addAttribute("lists", lists);
			model.addAttribute("articleUrl", articleUrl);
			model.addAttribute("pageIndexList", pageIndexList);
			model.addAttribute("dataCount", dataCount);
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "리스트 불러오는 중 에러가 발생했습니다.");
		}
	
		return "bbs/list";
	}
	
	@RequestMapping(value = "/article", 
			method = {RequestMethod.GET,RequestMethod.POST})
	public String article(HttpServletRequest request, Model model) {
		
		try {
			int num = Integer.parseInt(request.getParameter("num"));
			String pageNum = request.getParameter("pageNum");
			
			String searchKey = request.getParameter("searchKey");
			String searchValue = request.getParameter("searchValue");
			
			if(searchValue!=null) {
				searchValue = URLDecoder.decode(searchValue, "UTF-8");
			}
			
			boardService.updateHitCount(num);
			
			Board board = boardService.getReadData(num);
			
			if(board == null) {
				return "redirect:/list?pageNum=" + pageNum;
			}
			
			int lineSu = board.getContent().split("\n").length;
			
			
			String param = "pageNum=" + pageNum;
			if(searchValue!=null && !searchValue.equals("")) { //검색을 했다는뜻
				
				param += "&searchKey=" + searchKey;
				param += "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
			}
			
			model.addAttribute("board", board);
			model.addAttribute("params", param);
			model.addAttribute("lineSu", lineSu);
			model.addAttribute("pageNum", pageNum);
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "게시글을 불러오는 중 에러가 발생했습니다.");
		}
		return "bbs/article";
		
	}
	
	
	
	//get방식으로 request가 들어올때
	@RequestMapping(value = "/updated", method = RequestMethod.GET) //localhost로 접속
	public String updated(HttpServletRequest request, Model model) throws Exception {
		int num = Integer.parseInt(request.getParameter("num"));
	  	String pageNum = request.getParameter("pageNum");
	  	
		
	  	String searchKey = request.getParameter("searchKey");
	  	String searchValue = request.getParameter("searchValue");
	  	
	  	if(searchValue!=null) {
		  	searchValue = URLDecoder.decode(searchValue, "UTF-8");
	  	}
		
	  	Board board = boardService.getReadData(num);
	  	
		if(board==null) {
			return "redirect:/list?pageNum=" + pageNum;
		}
		
		String param = "pageNum=" + pageNum;
	
		if(searchValue!=null&&!searchValue.equals("")) {
			param += "&searchKey=" +searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");		
		}
		
		model.addAttribute("board", board);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("params", param);
		model.addAttribute("searchKey", searchKey);
		model.addAttribute("searchValue", searchValue);
		
		
		
		return "bbs/updated";
	}
	
	@RequestMapping(value = "/updated_ok", 
			method = {RequestMethod.GET,RequestMethod.POST})
	public String updatedOK(Board board, HttpServletRequest request, Model model){
		String pageNum = request.getParameter("pageNum");
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		String param = "?pageNum=" + pageNum;
		try {
			board.setContent(board.getContent().replaceAll( "<br/>", "\r\n"));
			boardService.updateData(board);
			
			
			if(searchValue!=null&&!searchValue.equals("")) {
				param += "&searchKey=" + searchKey;
				param += "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			try {
				param += "&errorMessage=" + URLEncoder.encode("게시물을 수정중 에러가 발생", "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		return "redirect:/list" + param;
		
  }
	
	@RequestMapping(value = "/deleted_ok", method ={RequestMethod.GET, RequestMethod.POST})
	public String deleteOK(HttpServletRequest request, Model model) {
		int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");
		String param = "?pageNum=" + pageNum;
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		
		try {
			
			boardService.deleteData(num);
			
			
			if(searchValue!=null&&!searchValue.equals("")) {
				param += "&searchKey=" + searchKey;
				param += "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			try {
				param += "&errorMessage=" + URLEncoder.encode("게시물을 삭제중 에러가 발생", "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		
		
		return "redirect:/list" + param;
		
	}
	
	
	
	
}
