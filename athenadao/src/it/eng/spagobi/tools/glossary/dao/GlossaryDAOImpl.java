package it.eng.spagobi.tools.glossary.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByParent;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWlistByContentId;
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
	public boolean insertGlossary(SbiGlGlossary glossary) {
		return saveOrUpdate(glossary);
	}

	@Override
	public boolean modifyGlossary(SbiGlGlossary glossary) {
		return saveOrUpdate(glossary);
	}

	@Override
	public boolean deleteGlossary(Integer glossaryId) {
		return delete(SbiGlGlossary.class, glossaryId);
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
	public boolean insertContents(SbiGlContents contents) {
		return saveOrUpdate(contents);
	}

	@Override
	public boolean modifyContents(SbiGlContents contents) {
		return saveOrUpdate(contents);
	}

	@Override
	public boolean deleteContents(Integer contentId) {
		return delete(SbiGlContents.class, contentId);
	}

	@Override
	public SbiGlWord loadWord(Integer wordId) {
		return load(SbiGlWord.class, wordId);
	}

	@Override
	public List<SbiGlWord> listWord(String word) {
		return list(new SearchWordByWord(word));
	}

	@Override
	public boolean insertWord(SbiGlWord word) {
		return saveOrUpdate(word);
	}

	@Override
	public boolean modifyWord(SbiGlWord word) {
		return saveOrUpdate(word);
	}

	@Override
	public boolean deleteWord(Integer wordId) {
		return delete(SbiGlWord.class, wordId);
	}

	@Override
	public List<SbiGlWlist> listWlist(Integer contentId) {
		return list(new SearchWlistByContentId(contentId));
	}

	@Override
	public boolean insertWlist(SbiGlWlist wlist) {
		return saveOrUpdate(wlist);
	}

	@Override
	public boolean modifyWlist(SbiGlWlist wlist) {
		return saveOrUpdate(wlist);
	}

	@Override
	public boolean deleteWlist(SbiGlWlistId wlistId) {
		return delete(SbiGlWlist.class, wlistId);
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
	public boolean insertAttribute(SbiGlAttribute attribute) {
		return saveOrUpdate(attribute);
	}

	@Override
	public boolean modifyAttribute(SbiGlAttribute attribute) {
		return saveOrUpdate(attribute);
	}

	@Override
	public boolean deleteAttribute(Integer attributeId) {
		return delete(SbiGlAttribute.class, attributeId);
	}

	@Override
	public List<SbiGlWordAttr> listWordAttr(Integer wordId) {
		return list(new SearchWordAttrByWordId(wordId));
	}

	@Override
	public boolean insertWordAttr(SbiGlWordAttr wordAttr) {
		return saveOrUpdate(wordAttr);
	}

	@Override
	public boolean modifyWordAttr(SbiGlWordAttr wordAttr) {
		return saveOrUpdate(wordAttr);
	}

	@Override
	public boolean deleteWordAttr(Integer wordId) {
		return delete(SbiGlWordAttr.class, wordId);
	}

}
