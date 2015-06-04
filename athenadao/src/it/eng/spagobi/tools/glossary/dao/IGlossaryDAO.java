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

	public boolean insertGlossary(SbiGlGlossary glossary);

	public boolean modifyGlossary(SbiGlGlossary glossary);

	public boolean deleteGlossary(Integer glossaryId);

	//
	// Contents
	//
	public SbiGlContents loadContents(Integer contentId);

	public List<SbiGlContents> listContents();

	public List<SbiGlContents> listContentsByGlossaryId(Integer glossaryId, Integer parentId);

	public boolean insertContents(SbiGlContents contents);

	public boolean modifyContents(SbiGlContents contents);

	public boolean deleteContents(Integer contentId);

	//
	// Word
	//
	public SbiGlWord loadWord(Integer wordId);

	public List<SbiGlWord> listWord(String word);

	public boolean insertWord(SbiGlWord word);

	public boolean modifyWord(SbiGlWord word);

	public boolean deleteWord(Integer wordId);

	//
	// Wishlist
	//
	public List<SbiGlWlist> listWlist(Integer contentId);

	public boolean insertWlist(SbiGlWlist wlist);

	public boolean modifyWlist(SbiGlWlist wlist);

	public boolean deleteWlist(SbiGlWlistId wlistId);

	//
	// Attribute
	//
	public SbiGlAttribute loadAttribute(Integer attributeId);

	public List<SbiGlAttribute> listAttribute();

	public boolean insertAttribute(SbiGlAttribute attribute);

	public boolean modifyAttribute(SbiGlAttribute attribute);

	public boolean deleteAttribute(Integer attributeId);

	//
	// WordAttr
	//
	public List<SbiGlWordAttr> listWordAttr(Integer wordId);

	public boolean insertWordAttr(SbiGlWordAttr wordAttr);

	public boolean modifyWordAttr(SbiGlWordAttr wordAttr);

	public boolean deleteWordAttr(Integer wordId);
}
