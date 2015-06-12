package it.eng.spagobi.tools.glossary.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByParent;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWlistByContentId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWord;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordAttrByWordId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordByWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlAttribute;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;

import java.util.List;

import org.apache.log4j.Logger;

public class GlossaryDAOImpl extends AbstractHibernateDAO implements IGlossaryDAO {

	static private Logger logger = Logger.getLogger(GlossaryDAOImpl.class);

	@Override
	public SbiGlGlossary loadGlossary(Integer id) {
		return load(SbiGlGlossary.class, id);
	}

	@Override
	public List<SbiGlGlossary> listGlossary() {
		return list(SbiGlGlossary.class);
	}

	@Override
	public Integer insertGlossary(SbiGlGlossary glossary) {
		return (Integer) insert(glossary);
	}

	@Override
	public void modifyGlossary(SbiGlGlossary glossary) {
		update(glossary);
	}

	@Override
	public void deleteGlossary(Integer glossaryId) {
		delete(SbiGlGlossary.class, glossaryId);
	}

	@Override
	public SbiGlContents loadContents(Integer contentId) {
		return load(SbiGlContents.class, contentId);
	}

	@Override
	public List<SbiGlContents> listContents() {
		return list(SbiGlContents.class);
	}

	@Override
	public List<SbiGlContents> listContentsByGlossaryId(Integer glossaryId, Integer parentId) {
		return list(new SearchContentsByParent(glossaryId, parentId));
	}

	@Override
	public Integer insertContents(SbiGlContents contents) {
		return (Integer) insert(contents);
	}

	@Override
	public void modifyContents(SbiGlContents contents) {
		update(contents);
	}

	@Override
	public void deleteContents(Integer contentId) {
		delete(SbiGlContents.class, contentId);
	}

	@Override
	public SbiGlWord loadWord(Integer wordId) {
		return load(SbiGlWord.class, wordId);
	}

	@Override
	public List<SbiGlWord> listWord() {
		return list(new SearchWord());
	}

	@Override
	public List<SbiGlWord> listWordFiltered(String word) {
		return list(new SearchWordByWord(word));
	}

	@Override
	public Integer insertWord(SbiGlWord word) {
		return (Integer) insert(word);
	}

	@Override
	public void modifyWord(SbiGlWord word) {
		update(word);
	}

	@Override
	public void deleteWord(Integer wordId) {
		delete(SbiGlWord.class, wordId);
	}

	@Override
	public List<SbiGlWlist> listWlist(Integer contentId) {
		return list(new SearchWlistByContentId(contentId));
	}

	@Override
	public Integer insertWlist(SbiGlWlist wlist) {
		return (Integer) insert(wlist);
	}

	@Override
	public void modifyWlist(SbiGlWlist wlist) {
		update(wlist);
	}

	@Override
	public void deleteWlist(SbiGlWlistId wlistId) {
		delete(SbiGlWlist.class, wlistId);
	}

	@Override
	public SbiGlAttribute loadAttribute(Integer attributeId) {
		return load(SbiGlAttribute.class, attributeId);
	}

	@Override
	public List<SbiGlAttribute> listAttribute() {
		return list(SbiGlAttribute.class);
	}

	@Override
	public Integer insertAttribute(SbiGlAttribute attribute) {
		return (Integer) insert(attribute);
	}

	@Override
	public void modifyAttribute(SbiGlAttribute attribute) {
		update(attribute);
	}

	@Override
	public void deleteAttribute(Integer attributeId) {
		delete(SbiGlAttribute.class, attributeId);
	}

	@Override
	public List<SbiGlWordAttr> listWordAttr(Integer wordId) {
		return list(new SearchWordAttrByWordId(wordId));
	}

	@Override
	public Integer insertWordAttr(SbiGlWordAttr wordAttr) {
		return (Integer) insert(wordAttr);
	}

	@Override
	public void modifyWordAttr(SbiGlWordAttr wordAttr) {
		update(wordAttr);
	}

	@Override
	public void deleteWordAttr(Integer wordId) {
		delete(SbiGlWordAttr.class, wordId);
	}

}
