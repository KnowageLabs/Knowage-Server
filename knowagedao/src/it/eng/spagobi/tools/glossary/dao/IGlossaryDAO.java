/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.glossary.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessCls;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessClsWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlTable;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
//import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * CRUD operations
 *
 * @author salvo l.
 *
 */
public interface IGlossaryDAO extends ISpagoBIDao {
	//
	// Glossary
	//
	public SbiGlGlossary loadGlossary(Integer glossaryId);

	public List<SbiGlGlossary> loadGlossaryByName(String glossaryNM);

	public List<SbiGlGlossary> listGlossary();

	public Integer insertGlossary(SbiGlGlossary glossary);

	public void modifyGlossary(SbiGlGlossary glossary);

	public void deleteGlossary(Integer glossaryId);

	public void cloneGlossary(Integer glossaryId, Integer newGlossId);

	public JSONObject glosstreeLike(String glossId, String word);

	//
	// Contents
	//
	public SbiGlContents loadContents(Integer contentId);

	public Integer CountContentChild(Integer contentId);

	public SbiGlContents loadContentsByParent(Integer contentId);

	public List<SbiGlContents> loadContentsByName(String contentNM);

	public List<SbiGlContents> listContents();

	public List<SbiGlContents> listContentsByGlossaryIdAndParentId(Integer glossaryId, Integer parentId);

	public Integer insertContents(SbiGlContents contents);

	public void modifyContents(SbiGlContents contents);

	public boolean modifyContentPosition(Integer contentId, Integer parentId, Integer glossaryId);

	public void deleteContents(Integer contentId);

	//
	// Word
	//
	public SbiGlWord loadWord(Integer wordId);

	public Integer wordCount(String word, Integer gloss);

	public List<SbiGlWord> loadWordByName(String wordNM);

	public List<SbiGlWord> listWord(Integer page, Integer item_per_page);

	public List<SbiGlWord> listWordFromArray(Object[] arr);

	public List<SbiGlWord> listWordFiltered(String word, Integer page, Integer item_per_page, Integer gloss_id);

	// public Integer insertWordOld(SbiGlWord word,List<SbiGlWord> objLink,List<SbiGlAttribute> objAttr,Map<Integer, JSONObject> MapAttr,Map<Integer,
	// JSONObject> MapLink,final boolean modify);

	public Integer insertWord(SbiGlWord word, List<SbiGlWord> objLink, List<SbiUdp> objAttr, Map<Integer, JSONObject> MapAttr,
			Map<Integer, JSONObject> MapLink, final boolean modify);

	public void modifyWord(SbiGlWord word);

	public void deleteWord(Integer wordId);

	//
	// Wishlist
	//
	public List<SbiGlWlist> listWlist(Integer contentId);

	public Integer CountWlistByContent(Integer contentId);

	public List<SbiGlWord> listWlistWord(Integer contentId);

	public SbiGlWlist loadWlist(SbiGlWlistId listId);

	public SbiGlWlistId insertWlist(SbiGlWlist wlist);

	public void modifyWlist(SbiGlWlist wlist);

	public void deleteWlist(SbiGlWlistId wlistId);

	public List<SbiGlWlist> listWlistByGlossaryIdAndWordId(Integer glossaryId, Integer wordId);

	// //
	// // Attribute
	// //
	// public SbiGlAttribute loadAttribute(Integer attributeId);
	//
	// public List<SbiGlAttribute> listAttribute();
	//
	// public List<SbiGlAttribute> listAttrFromArray(Object[] arr);
	//
	// public List<SbiGlAttribute> listAttributeFiltered(String attribute);
	//
	// public Integer insertAttribute(SbiGlAttribute attribute);
	//
	// public void modifyAttribute(SbiGlAttribute attribute);
	//
	// public void deleteAttribute(Integer attributeId);

	//
	// WordAttr
	//
	public List<SbiUdpValue> listWordAttr(Integer wordId);

	public Integer insertWordAttr(SbiUdpValue wordAttr);

	public void modifyWordAttr(SbiUdpValue wordAttr);

	public void deleteWordAttr(Integer wordId);

	// references
	public void deleteWordReferences(Integer wordId);

	// docWlist
	public List<SbiGlDocWlist> listDocWlist(Integer docwId);

	public SbiGlDocWlist loadDocWlist(SbiGlDocWlistId docwlistId);

	public SbiGlDocWlistId insertDocWlist(SbiGlDocWlist docwlist);

	public SbiGlDocWlist getDocWlistOrNull(SbiGlDocWlistId id);

	public void deleteDocWlist(SbiGlDocWlistId id);

	// datasetWlist
	public List<SbiGlDataSetWlist> listDataSetWlist(Integer datasetId, String Organiz);

	public SbiGlDataSetWlist getDataSetWlistOrNull(SbiGlDataSetWlistId id);

	public SbiGlDataSetWlistId insertDataSetWlist(SbiGlDataSetWlist docwlist);

	public void deleteDataSetWlist(SbiGlDataSetWlistId id);

	public List<SbiGlDataSetWlist> loadDataSetWlist(Integer datasetId, String Organiz);

	// navigation
	public Map<String, Object> NavigationItem(JSONObject elem);

	//
	// Business Class
	//
	public SbiGlBnessCls loadBnessCls(Integer bcId);

	public List<SbiGlBnessCls> loadBnessClassByParameter(String datamart, String bness_cls);

	public List<SbiGlBnessClsWlist> loadBnessClsWlistByParameter(Integer id, String column);

	//
	// Business Class
	//
	public SbiGlTable loadTable(Integer tableId);

}
