package it.eng.spagobi.tools.glossary.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchAttributeByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByParent;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchGlossaryByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWlistByContentId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWord;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordAttrByWordId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordByName;
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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GlossaryDAOImpl extends AbstractHibernateDAO implements
		IGlossaryDAO {

	static private Logger logger = Logger.getLogger(GlossaryDAOImpl.class);

	@Override
	public SbiGlGlossary loadGlossary(Integer id) {
		return load(SbiGlGlossary.class, id);
	}

	@Override
	public List<SbiGlWord> loadWordByName(String name) {
		return list(new SearchWordByName(name));

	}

	@Override
	public List<SbiGlGlossary> loadGlossaryByName(String name) {
		return list(new SearchGlossaryByName(name));

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
	public void deleteGlossary(final Integer glossaryId) {

		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {

			@Override
			public Boolean execute(Session session) {

				List<SbiGlContents> lc=listContentsByGlossaryIdAndParentId(glossaryId,null);
				
				for(SbiGlContents sb:lc){
					deleteContents(sb.getContentId());
				}
				delete(SbiGlGlossary.class, glossaryId);
				return true;
			}
		});

	}

	@Override
	public SbiGlContents loadContents(Integer contentId) {
		return load(SbiGlContents.class, contentId);
	}

	@Override
	public List<SbiGlContents> loadContentsByName(String contentNM) {
		return list(new SearchContentsByName(contentNM));

	}

	@Override
	public List<SbiGlContents> listContents() {
		return list(SbiGlContents.class);
	}

	@Override
	public List<SbiGlContents> listContentsByGlossaryIdAndParentId(
			Integer glossaryId, Integer parentId) {
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
	public void modifyContentPosition(Integer contentId, Integer parentId,
			Integer glossaryId) {
		SbiGlContents content = load(SbiGlContents.class, contentId);
		content.setParentId(parentId);
		content.setGlossaryId(glossaryId);
		update(content);
	}

	@Override
	public void deleteContents(final Integer contentId) {

		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {

			@Override
			public Boolean execute(Session session) {

				List<SbiGlContents> att = listContentsByGlossaryIdAndParentId(
						null, contentId);
				if (!att.isEmpty()) {
					for (SbiGlContents tmp : att) {
						deleteContents(tmp.getContentId());
					}
				} else {
					// check if have word references
					Query q = session
							.createQuery("delete from SbiGlWlist  where content.contentId="
									+ contentId);
					q.executeUpdate();
				}

				Object obj = session.get(SbiGlContents.class, contentId);
				if (obj != null) {
					session.delete(obj);
				}
				return true;
			}

		});
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
	public Integer insertWord(final SbiGlWord word,
			final List<SbiGlWord> objLink, final List<SbiGlAttribute> objAttr,
			final Map<Integer, JSONObject> MapAttr,
			final Map<Integer, JSONObject> MapLink, final boolean modify) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) {

				Integer wordId;

				Boolean doUpdate = false;
				if (modify) {
					doUpdate = true;

					wordId = word.getWordId();
				} else {
					updateSbiCommonInfo4Insert(word);
					wordId = (Integer) session.save(word);
				}

				if (objLink != null) {
					doUpdate = true;
					Set<SbiGlReferences> references = new HashSet<SbiGlReferences>();
					if (word.getReferences() == null) {
						word.setReferences(new HashSet<SbiGlReferences>());
					}

					for (SbiGlWord w : objLink) {

						if (modify) {
							// check if user modify value or order of presents
							// link
							boolean pres = false;
							for (SbiGlReferences at : word.getReferences()) {
								if (at.getId().getRefWordId() == w.getWordId()) {
									pres = true;
									try {
										if (at.getSequence() != MapLink.get(
												w.getWordId()).getInt("ORDER")) {
											// alter index
											at.setSequence(MapLink.get(
													w.getWordId()).getInt(
													"ORDER"));
											updateSbiCommonInfo4Update(at);
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();

									}
									references.add(at);
									break;
								}

							}

							if (pres) {
								continue;
							}
						}

						// if references link there isn't, create it;

						SbiGlReferences tmp = new SbiGlReferences();
						tmp.setId(new SbiGlReferencesId(wordId, w.getWordId()));
						tmp.setWord(word);
						tmp.setRefWord(w);

						try {
							tmp.setSequence(MapLink.get(w.getWordId()).getInt(
									"ORDER"));

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						updateSbiCommonInfo4Insert(tmp);
						references.add(tmp);
						word.getReferences().add(tmp);

					}

					// remove the references link not present in new list
					word.getReferences().retainAll(references);

				} else {

					if (word.getReferences() != null
							&& word.getReferences().size() != 0) {
						word.getReferences().clear();
						doUpdate = true;
					}

				}

				if (objAttr != null) {
					Set<SbiGlWordAttr> SbiGlWordAttr = new HashSet<SbiGlWordAttr>();
					if (word.getAttributes() == null) {
						word.setAttributes(new HashSet<SbiGlWordAttr>());
					}

					doUpdate = true;

					for (SbiGlAttribute w : objAttr) {

						if (modify) {
							// check if user modify value or order of presents
							// attribute
							boolean pres = false;
							for (SbiGlWordAttr at : word.getAttributes()) {
								if (at.getId().getAttributeId() == w
										.getAttributeId()
										&& at.getId().getWordId() == wordId) {

									pres = true;
									try {
										boolean alterAttr = false;
										if (at.getValue().compareTo(
												MapAttr.get(w.getAttributeId())
														.getString("VALUE")) != 0) {
											// alter value
											at.setValue(MapAttr.get(
													w.getAttributeId())
													.getString("VALUE"));
											alterAttr = true;
										}
										if (at.getOrder() != MapAttr.get(
												w.getAttributeId()).getInt(
												"ORDER")) {
											// alter index
											at.setOrder(MapAttr.get(
													w.getAttributeId()).getInt(
													"ORDER"));
											alterAttr = true;
										}
										if (alterAttr) {
											updateSbiCommonInfo4Update(at);
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();

									}

									SbiGlWordAttr.add(at);
									break;
								}
							}
							if (pres) {
								continue;
							}
						}

						// if attribute there isn't, create it;

						SbiGlWordAttr tmp = new SbiGlWordAttr();
						tmp.setId(new SbiGlWordAttrId(wordId, w
								.getAttributeId()));
						tmp.setWord(word);
						tmp.setAttribute(w);

						try {
							tmp.setValue(MapAttr.get(w.getAttributeId())
									.getString("VALUE"));

							tmp.setOrder(MapAttr.get(w.getAttributeId())
									.getInt("ORDER"));

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						updateSbiCommonInfo4Insert(tmp);
						SbiGlWordAttr.add(tmp);
						word.getAttributes().add(tmp);

					}

					// remove the attribute not present in new list
					word.getAttributes().retainAll(SbiGlWordAttr);

				} else {
					// remove all attribute if there aren't in new list
					if (word.getAttributes() != null
							&& word.getAttributes().size() != 0) {
						word.getAttributes().clear();
						doUpdate = true;
					}
				}

				if (doUpdate) {
					updateSbiCommonInfo4Update(word);
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
	public List<SbiGlWord> listWlistWord(Integer contentId) {
		Session session = getSession();
		Query q = session
				.createQuery("select wl.word from SbiGlWlist wl where wl.content.contentId="
						+ contentId);
		List<SbiGlWord> a = (List<SbiGlWord>) q.list();

		return a;
	}

	@Override
	public SbiGlWlistId insertWlist(SbiGlWlist wlist) {
		return (SbiGlWlistId) insert(wlist);
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

	@Override
	public void deleteWordReferences(Integer wordId) {
		deleteWordRef(wordId);
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
