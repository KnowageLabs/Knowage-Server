package it.eng.knowage.boot.filter;

import java.util.List;

public interface IWhiteList {

	List<String> getRelativePaths();

	List<String> getExternalServices();

}