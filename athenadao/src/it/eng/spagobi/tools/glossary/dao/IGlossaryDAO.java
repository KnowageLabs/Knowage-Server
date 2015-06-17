package it.eng.spagobi.tools.glossary.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.glossary.metadata.SbiGlAttribute;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;

import java.util.List;

import org.json.JSONArray;

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

	public List<SbiGlGlossary> listGlossary();

	public Integer insertGlossary(SbiGlGlossary glossary);

	public void modifyGlossary(SbiGlGlossary glossary);

	public void deleteGlossary(Integer glossaryId);

	//
	// Contents
	//
	public SbiGlContents loadContents(Integer contentId);

	public List<SbiGlContents> listContents();

	public List<SbiGlContents> listContentsByGlossaryId(Integer glossaryId, Integer parentId);

	public Integer insertContents(SbiGlContents contents);

	public void modifyContents(SbiGlContents contents);

	public void deleteContents(Integer contentId);

	//
	// Word
	//
	public SbiGlWord loadWord(Integer wordId);

	public List<SbiGlWord> listWord();
	
	public List<SbiGlWord> listWordFromArray(Object[] arr);

	public List<SbiGlWord> listWordFiltered(String word);

	public Integer insertWord(SbiGlWord word,List<SbiGlWord> objLink,List<SbiGlAttribute> objAttr,JSONArray jarr);

	public void modifyWord(SbiGlWord word);

	public void deleteWord(Integer wordId);

	//
	// Wishlist
	//
	public List<SbiGlWlist> listWlist(Integer contentId);

	public Integer insertWlist(SbiGlWlist wlist);

	public void modifyWlist(SbiGlWlist wlist);

	public void deleteWlist(SbiGlWlistId wlistId);

	//
	// Attribute
	//
	public SbiGlAttribute loadAttribute(Integer attributeId);

	public List<SbiGlAttribute> listAttribute();

	public List<SbiGlAttribute> listAttrFromArray(Object[] arr);
	
	public List<SbiGlAttribute> listAttributeFiltered(String attribute);
	
	public Integer insertAttribute(SbiGlAttribute attribute);

	public void modifyAttribute(SbiGlAttribute attribute);

	public void deleteAttribute(Integer attributeId);

	//
	// WordAttr
	//
	public List<SbiGlWordAttr> listWordAttr(Integer wordId);

	public Integer insertWordAttr(SbiGlWordAttr wordAttr);

	public void modifyWordAttr(SbiGlWordAttr wordAttr);

	public void deleteWordAttr(Integer wordId);
}
