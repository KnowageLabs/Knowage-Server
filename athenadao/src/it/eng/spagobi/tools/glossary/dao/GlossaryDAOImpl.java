package it.eng.spagobi.tools.glossary.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchAttributeByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByParent;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWlistByContentId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWord;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordAttrByWordId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordByWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlAttribute;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferences;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferencesId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttrId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;

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
	public void modifyContentPosition(Integer contentId, Integer parentId, Integer glossaryId) {
		SbiGlContents content = load(SbiGlContents.class, contentId);
		content.setParentId(parentId);
		content.setGlossaryId(glossaryId);
		update(content);
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
	public Integer insertWord(final SbiGlWord word, final List<SbiGlWord> objLink, final List<SbiGlAttribute> objAttr, final JSONArray jarr) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) {
				updateSbiCommonInfo4Insert(word);
				Integer wordId = (Integer) session.save(word);
				if (objLink != null) {
					Set<SbiGlReferences> references = new HashSet<SbiGlReferences>();

					int index = 0;
					for (SbiGlWord w : objLink) {
						SbiGlReferences tmp = new SbiGlReferences();
						tmp.setId(new SbiGlReferencesId(wordId, w.getWordId()));
						tmp.setWord(word);
						tmp.setRefWord(w);
						tmp.setSequence(index);
						updateSbiCommonInfo4Update(tmp);
						references.add(tmp);
						index++;
					}

					if (!references.isEmpty()) {
						word.setReferences(references);
					} else {
						word.setReferences(null);
					}
				}

				if (objAttr != null) {
					Set<SbiGlWordAttr> SbiGlWordAttr = new HashSet<SbiGlWordAttr>();

					int index = 0;
					for (SbiGlAttribute w : objAttr) {
						SbiGlWordAttr tmp = new SbiGlWordAttr();
						tmp.setId(new SbiGlWordAttrId(wordId, w.getAttributeId()));
						tmp.setWord(word);
						tmp.setAttribute(w);

						for (int i = 0; i < jarr.length(); i++) {

							try {
								if (jarr.getJSONObject(i).getInt("ATTRIBUTE_ID") == w.getAttributeId()) {
									tmp.setValue(jarr.getJSONObject(i).getString("VALUE"));
									jarr.remove(i);
									break;
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						tmp.setOrder(index);
						updateSbiCommonInfo4Update(tmp);
						SbiGlWordAttr.add(tmp);
						index++;
					}

					if (!SbiGlWordAttr.isEmpty()) {
						word.setAttributes(SbiGlWordAttr);
					} else {
						word.setAttributes(null);
					}

					session.update(word);
				}
				return wordId;
			}
		});
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
	public List<SbiGlAttribute> listAttributeFiltered(String attribute) {
		return list(new SearchAttributeByName(attribute));
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SbiGlWord> listWordFromArray(final Object[] arr) {
		return list(new ICriterion() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiGlWord.class);
				c.add(Restrictions.in("wordId", arr));
				return c;
			}
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SbiGlAttribute> listAttrFromArray(final Object[] arr) {
		return list(new ICriterion() {

			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiGlAttribute.class);
				c.add(Restrictions.in("attributeId", arr));
				return c;
			}
		});
	}

}
