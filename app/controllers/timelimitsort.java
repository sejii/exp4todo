package controllers;

import java.util.Comparator;
import models.*;

public class timelimitsort implements Comparator<TodoList>{
	public int compare(TodoList a, TodoList b){
	 	int no1 = a.getTL();
	 	int no2 = b.getTL();
	 	
	 	if(no1 > no2){
	 		return 1;
	 	}else if(no1 == no2){
	 		return 0;
	 	}else{
	 		return -1;
	 	}
	 }
}