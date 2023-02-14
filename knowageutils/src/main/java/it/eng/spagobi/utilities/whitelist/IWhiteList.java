package it.eng.spagobi.utilities.whitelist;

import java.util.List;

public interface IWhiteList {

	List<String> getRelativePaths();

	List<String> getExternalServices();

}