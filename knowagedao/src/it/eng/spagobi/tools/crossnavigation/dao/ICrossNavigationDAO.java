package it.eng.spagobi.tools.crossnavigation.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;

import java.util.List;

public interface ICrossNavigationDAO extends ISpagoBIDao {

	public List<SimpleNavigation> listNavigation();

	public NavigationDetail loadNavigation(Integer id);

	public void update(NavigationDetail nd);

	public void insert(NavigationDetail nd);

	public void delete(Integer id);

}
