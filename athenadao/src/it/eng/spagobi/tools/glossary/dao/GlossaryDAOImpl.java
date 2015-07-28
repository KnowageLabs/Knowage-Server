package it.eng.spagobi.tools.glossary.dao;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchContentsByParent;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchGlossaryByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchGlossaryStructureWithWordLike;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchListDataSetWlist;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchListDocWlist;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWlistByContentId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWord;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordAttrByWordId;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordByName;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchWordByWord;
import it.eng.spagobi.tools.glossary.dao.criterion.SearchtWlistByGlossaryIdAndWordId;
import it.eng.spagobi.tools.glossary.dao.criterion.loadContentsParent;
import it.eng.spagobi.tools.glossary.dao.criterion.loadDocWlistByDocumentAndWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessCls;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferences;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferencesId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlTable;
//import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;
//import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttrId;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.transform.Transformers;
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

				List<SbiGlContents> lc = listContentsByGlossaryIdAndParentId(
						glossaryId, null);

				for (SbiGlContents sb : lc) {
					deleteContents(sb.getContentId());
				}
				delete(SbiGlGlossary.class, glossaryId);
				return true;
			}
		});

	}

	@Override
	public void cloneGlossary(final Integer glossaryId, final Integer newGlossId) {

		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {

			@Override
			public Boolean execute(Session session) {

				// //get glossary
				// SbiGlGlossary glo=loadGlossary(glossaryId);
				// if (glo==null) {
				// return false;
				// }
				// glo.setGlossaryNm(glo.getGlossaryNm()+"-copy");
				// Integer newGlossId =insertGlossary(glo);

				Map<Integer, Integer> newID = new HashMap<Integer, Integer>();

				List<SbiGlContents> toClone = new ArrayList<SbiGlContents>();
				// add first level child
				toClone.addAll(listContentsByGlossaryIdAndParentId(glossaryId,
						null));

				while (!toClone.isEmpty()) {
					SbiGlContents tmp = toClone.remove(0);
					tmp.setGlossaryId(newGlossId);
					tmp.setParentId(newID.get(tmp.getParentId()));
					Integer oldID = tmp.getContentId();
					Integer ni = insertContents(tmp);
					newID.put(oldID, ni);

					List<SbiGlContents> chlis = listContentsByGlossaryIdAndParentId(
							glossaryId, oldID);
					for (SbiGlContents glc : chlis) {
						toClone.add(glc);
					}
					if (chlis.isEmpty()) {
						// check if have node child
						List<SbiGlWord> wl = listWlistWord(oldID);
						for (SbiGlWord sgw : wl) {
							SbiGlWlist contw = new SbiGlWlist();
							contw.setId(new SbiGlWlistId(sgw.getWordId(), ni));
							insertWlist(contw);
						}
					}

				}

				return true;
			}
		});

	}

	public static JSONObject fromContentsLight(SbiGlContents sbiGlContents,
			boolean wordChild, boolean ContentsChild) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("CONTENT_ID", sbiGlContents.getContentId());
		ret.put("CONTENT_NM", sbiGlContents.getContentNm());
		ret.put("HAVE_WORD_CHILD", wordChild);
		ret.put("HAVE_CONTENTS_CHILD", ContentsChild);
		ret.put("CHILD", new JSONArray());
		return ret;
	}

	private static JSONObject fromWordLight(SbiGlWord sbiGlWord)
			throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("WORD_ID", sbiGlWord.getWordId());
		jobj.put("WORD", sbiGlWord.getWord());
		return jobj;
	}

	private void addAll(JSONArray a,JSONArray b) throws JSONException{
		for(int i=0;i<b.length();i++){
			a.put(b.get(i));
		}
	}
	private JSONArray createTreeStructureFromMap(JSONArray start,
			Map<String, JSONObject> map) throws JSONException {
		JSONArray fin = new JSONArray();
		while (start.length() != 0) {
			String tmp = start.remove(0).toString();
			// recupero l'oggetto
			JSONObject jo = map.get(tmp);
			// recupero i suoi figli

			JSONArray jaf = new JSONArray();
			
			if(jo.has("CHILD")){
			JSONArray ch = jo.getJSONArray("CHILD");
			
				for (int i = 0; i < ch.length(); i++) {
					if(jo.getBoolean("HAVE_CONTENTS_CHILD")){
					addAll(jaf,createTreeStructureFromMap(map.get(ch.get(i)).getJSONArray("CHILD"), map));
					}else{
						jaf.put(map.get(ch.get(i)));
					}
				}
				
			
			
			jo.put("CHILD", jaf);
			
			}
			fin.put(jo);

		}

		return fin;
	}

	@Override
	public JSONObject glosstreeLike(final String glossId, final String word) {
		return executeOnTransaction(new IExecuteOnTransaction<JSONObject>() {
			@Override
			public JSONObject execute(Session session) {
				JSONObject jo = new JSONObject();
				try {

					SbiGlGlossary glo = loadGlossary(Integer.parseInt(glossId));

					jo.put("GLOSSARY_ID", glo.getGlossaryId());
					jo.put("GLOSSARY_NM", glo.getGlossaryNm());
					jo.put("SBI_GL_CONTENTS", new JSONArray());
					//

					List<SbiGlWlist> wordl = list(new SearchGlossaryStructureWithWordLike(
							glossId, word));
					// Map<Integer,SbiGlContents> cont = new
					// HashMap<Integer,SbiGlContents>();

					Map<String, JSONObject> map = new HashMap<String, JSONObject>();

					for (SbiGlWlist wl : wordl) {
						// cont.put(wl.getContent().getContentId(),
						// wl.getContent());

						// first element have word child and haven't logical
						// children

						map.put("W-" + wl.getWord().getWordId(),
								fromWordLight(wl.getWord()));

						if (!map.containsKey("L-"+ wl.getContent().getContentId())) {
							JSONObject tmpwl = fromContentsLight(wl.getContent(), true, false);
							tmpwl.getJSONArray("CHILD").put("W-" + wl.getWord().getWordId());
							// add logical node
							map.put("L-" + wl.getContent().getContentId(),tmpwl);
						} else {
							map.get("L-" + wl.getContent().getContentId()).getJSONArray("CHILD").put("W-" + wl.getWord().getWordId());
						}

						SbiGlContents par;
						String child = "L-" + wl.getContent().getContentId();
						do {
							par = loadContentsByParent(Integer.parseInt(child
									.split("-")[1]));
							if (par != null) {
								if (map.containsKey("L-" + par.getContentId())) {
									if (map.get("L-" + par.getContentId())
											.getJSONArray("CHILD").toString()
											.indexOf(child) == -1) {
										// add a node to present parent
										map.get("L-" + par.getContentId())
												.getJSONArray("CHILD")
												.put(child);
									}
								} else {
									// parent non present in map
									JSONObject tmp = fromContentsLight(par,
											false, true);
									tmp.getJSONArray("CHILD").put(child);
									map.put("L-" + par.getContentId(), tmp);
								}
								child = "L-" + par.getContentId();

							}

						} while (par != null);

						if (jo.getJSONArray("SBI_GL_CONTENTS").toString()
								.indexOf(child) == -1) {
							jo.getJSONArray("SBI_GL_CONTENTS").put(child);
						}

					}

					JSONArray fin = createTreeStructureFromMap(
							jo.getJSONArray("SBI_GL_CONTENTS"), map);
					jo.put("SBI_GL_CONTENTS", fin);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return jo;
			}

		});

	}

	@Override
	public SbiGlContents loadContents(Integer contentId) {
		return load(SbiGlContents.class, contentId);
	}

	@Override
	public SbiGlContents loadContentsByParent(Integer contentId) {
		List<SbiGlContents> l = list(new loadContentsParent(contentId));
		if (l.isEmpty()) {
			return null;
		} else {
			return l.get(0).getParent();
		}
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
	public Integer CountContentChild(final Integer contentId) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) {
				
		return ((Long) session.createCriteria(SbiGlContents.class).setProjection(Projections.rowCount()).add(Restrictions.eq("parent.contentId", contentId)).uniqueResult()).intValue();
	
			}
			
		});
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
	public boolean modifyContentPosition(Integer contentId, Integer parentId,
			Integer glossaryId) {
		SbiGlContents content = load(SbiGlContents.class, contentId);
		List<SbiGlContents> parent = listContentsByGlossaryIdAndParentId(
				glossaryId, parentId);
		for (SbiGlContents sb : parent) {
			if (sb.getContentNm().toLowerCase().trim()
					.compareTo(content.getContentNm().toLowerCase().trim()) == 0) {
				return false;
			}
		}

		content.setParentId(parentId);
		content.setGlossaryId(glossaryId);
		update(content);
		return true;
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
	public Integer wordCount(final String word, final Integer glossary_id) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) {
				if ((word != null && !word.trim().isEmpty())
						|| glossary_id != null) {

					Criteria c;

					if (glossary_id == null) {
						c = session.createCriteria(SbiGlWord.class, "gl_word");
					} else {
						// filter by glossary
						c = session.createCriteria(SbiGlWlist.class, "wlist");
						c.createAlias("wlist.word", "gl_word");
						c.createAlias("wlist.content", "gl_cont");
						c.add(Restrictions
								.eq("gl_cont.glossaryId", glossary_id));
					}

					if (word != null && !word.isEmpty()) {
						c.add(Restrictions.like("gl_word.word", word,
								MatchMode.ANYWHERE).ignoreCase());
					}

					c.setProjection(Projections.rowCount());

					return ((Long) c.uniqueResult()).intValue();

				}

				return ((Long) session.createCriteria(SbiGlWord.class)
						.setProjection(Projections.rowCount()).uniqueResult())
						.intValue();

			}

		});
			}
	
	@Override
	public List<SbiGlWord> listWord(Integer page,Integer item_per_page) {
		return list(new SearchWord( page, item_per_page));
	}

	@Override
	public List<SbiGlWord> listWordFiltered(String word,Integer page,Integer item_per_page,Integer gloss_id) {
		return list(new SearchWordByWord(word,page, item_per_page,gloss_id));
	}

//	@Override
//	public Integer insertWordOld(final SbiGlWord word,
//			final List<SbiGlWord> objLink, final List<SbiGlAttribute> objAttr,
//			final Map<Integer, JSONObject> MapAttr,
//			final Map<Integer, JSONObject> MapLink, final boolean modify) {
//		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
//			@Override
//			public Integer execute(Session session) {
//
//				Integer wordId;
//
//				Boolean doUpdate = false;
//				if (modify) {
//					doUpdate = true;
//
//					wordId = word.getWordId();
//				} else {
//					updateSbiCommonInfo4Insert(word);
//					wordId = (Integer) session.save(word);
//				}
//
//				if (objLink != null) {
//					doUpdate = true;
//					Set<SbiGlReferences> references = new HashSet<SbiGlReferences>();
//					if (word.getReferences() == null) {
//						word.setReferences(new HashSet<SbiGlReferences>());
//					}
//
//					for (SbiGlWord w : objLink) {
//
//						if (modify) {
//							// check if user modify value or order of presents
//							// link
//							boolean pres = false;
//							for (SbiGlReferences at : word.getReferences()) {
//								if (at.getId().getRefWordId() == w.getWordId()) {
//									pres = true;
//									try {
//										if (at.getSequence() != MapLink.get(
//												w.getWordId()).getInt("ORDER")) {
//											// alter index
//											at.setSequence(MapLink.get(
//													w.getWordId()).getInt(
//													"ORDER"));
//											updateSbiCommonInfo4Update(at);
//										}
//									} catch (JSONException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//
//									}
//									references.add(at);
//									break;
//								}
//
//							}
//
//							if (pres) {
//								continue;
//							}
//						}
//
//						// if references link there isn't, create it;
//
//						SbiGlReferences tmp = new SbiGlReferences();
//						tmp.setId(new SbiGlReferencesId(wordId, w.getWordId()));
//						tmp.setWord(word);
//						tmp.setRefWord(w);
//
//						try {
//							tmp.setSequence(MapLink.get(w.getWordId()).getInt(
//									"ORDER"));
//
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//						updateSbiCommonInfo4Insert(tmp);
//						references.add(tmp);
//						word.getReferences().add(tmp);
//
//					}
//
//					// remove the references link not present in new list
//					word.getReferences().retainAll(references);
//
//				} else {
//
//					if (word.getReferences() != null
//							&& word.getReferences().size() != 0) {
//						word.getReferences().clear();
//						doUpdate = true;
//					}
//
//				}
//
//				if (objAttr != null) {
//					Set<SbiGlWordAttr> SbiGlWordAttr = new HashSet<SbiGlWordAttr>();
//					if (word.getAttributes() == null) {
//						word.setAttributes(new HashSet<SbiGlWordAttr>());
//					}
//
//					doUpdate = true;
//
//					for (SbiGlAttribute w : objAttr) {
//
//						if (modify) {
//							// check if user modify value or order of presents
//							// attribute
//							boolean pres = false;
//							for (SbiGlWordAttr at : word.getAttributes()) {
//								if (at.getId().getAttributeId() == w
//										.getAttributeId()
//										&& at.getId().getWordId() == wordId) {
//
//									pres = true;
//									try {
//										boolean alterAttr = false;
//										if (at.getValue().compareTo(
//												MapAttr.get(w.getAttributeId())
//														.getString("VALUE")) != 0) {
//											// alter value
//											at.setValue(MapAttr.get(
//													w.getAttributeId())
//													.getString("VALUE"));
//											alterAttr = true;
//										}
//										if (at.getOrder() != MapAttr.get(
//												w.getAttributeId()).getInt(
//												"ORDER")) {
//											// alter index
//											at.setOrder(MapAttr.get(
//													w.getAttributeId()).getInt(
//													"ORDER"));
//											alterAttr = true;
//										}
//										if (alterAttr) {
//											updateSbiCommonInfo4Update(at);
//										}
//									} catch (JSONException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//
//									}
//
//									SbiGlWordAttr.add(at);
//									break;
//								}
//							}
//							if (pres) {
//								continue;
//							}
//						}
//
//						// if attribute there isn't, create it;
//
//						SbiGlWordAttr tmp = new SbiGlWordAttr();
//						tmp.setId(new SbiGlWordAttrId(wordId, w
//								.getAttributeId()));
//						tmp.setWord(word);
//						tmp.setAttribute(w);
//
//						try {
//							tmp.setValue(MapAttr.get(w.getAttributeId())
//									.getString("VALUE"));
//
//							tmp.setOrder(MapAttr.get(w.getAttributeId())
//									.getInt("ORDER"));
//
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//						updateSbiCommonInfo4Insert(tmp);
//						SbiGlWordAttr.add(tmp);
//						word.getAttributes().add(tmp);
//
//					}
//
//					// remove the attribute not present in new list
//					word.getAttributes().retainAll(SbiGlWordAttr);
//
//				} else {
//					// remove all attribute if there aren't in new list
//					if (word.getAttributes() != null
//							&& word.getAttributes().size() != 0) {
//						word.getAttributes().clear();
//						doUpdate = true;
//					}
//				}
//
//				if (doUpdate) {
//					updateSbiCommonInfo4Update(word);
//					session.update(word);
//				}
//				return wordId;
//			}
//		});
//	}

	
	@Override
	public Integer insertWord(final SbiGlWord word,
			final List<SbiGlWord> objLink, final List<SbiUdp> objAttr,
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
					Set<SbiUdpValue> SbiUdpValue = new HashSet<SbiUdpValue>();
					if (word.getAttributes() == null) {
						word.setAttributes(new HashSet<SbiUdpValue>());
					}

					doUpdate = true;

					for (SbiUdp w : objAttr) {

						if (modify) {
							// check if user modify value or order of presents
							// attribute
							boolean pres = false;
							for (SbiUdpValue at : word.getAttributes()) {
								if (at.getSbiUdp().getUdpId()== w.getUdpId() && at.getReferenceId().intValue() == wordId.intValue()) {
									pres = true;
									try {
										boolean alterAttr = false;
										if (at.getValue().compareTo(
												MapAttr.get(w.getUdpId()).getString("VALUE")) != 0) {
											// alter value
											at.setValue(MapAttr.get(
													w.getUdpId())
													.getString("VALUE"));
											alterAttr = true;
										}
//										if (at.getOrder() != MapAttr.get(
//												w.getUdpId()).getInt(
//												"ORDER")) {
//											// alter index
//											at.setOrder(MapAttr.get(
//													w.getAttributeId()).getInt(
//													"ORDER"));
//											alterAttr = true;
//										}
										if (alterAttr) {
											updateSbiCommonInfo4Update(at);
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();

									}

									SbiUdpValue.add(at);
									break;
								}
							}
							if (pres) {
								continue;
							}
						}

						// if attribute there isn't, create it;

						SbiUdpValue tmp = new SbiUdpValue();
						tmp.setSbiUdp(w);
						tmp.setReferenceId(word.getWordId());
						
//						tmp.setId(new SbiGlWordAttrId(wordId, w
//								.getAttributeId()));
//						tmp.setWord(word);
//						tmp.setAttribute(w);

						try {
							tmp.setValue(MapAttr.get(w.getUdpId())
									.getString("VALUE"));

//							tmp.setOrder(MapAttr.get(w.getAttributeId())
//									.getInt("ORDER"));

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						updateSbiCommonInfo4Insert(tmp);
						SbiUdpValue.add(tmp);
						word.getAttributes().add(tmp);

					}

					// remove the attribute not present in new list
					word.getAttributes().retainAll(SbiUdpValue);

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
	public void deleteWord(final Integer wordId) {

		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {

			@Override
			public Boolean execute(Session session) {

				Query q = session
						.createQuery("delete from SbiGlReferences where refWord.wordId=:id");
				q.setParameter("id", wordId);
				q.executeUpdate();

				Query q3 = session
						.createQuery("delete from SbiGlDocWlist  where id.wordId=:id");
				q3.setParameter("id", wordId);
				q3.executeUpdate();
				
				Query q2 = session
						.createQuery("delete from SbiGlWlist  where word.wordId=:id");
				q2.setParameter("id", wordId);
				q2.executeUpdate();

				
				
				Object obj = session.get(SbiGlWord.class, wordId);
				if (obj != null) {
					session.delete(obj);
				}
				return true;
			}
		});

	}

	@Override
	public List<SbiGlWlist> listWlist(Integer contentId) {
		return list(new SearchWlistByContentId(contentId));
	}
	
	@Override
	public Integer CountWlistByContent(final Integer contentId) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) {
				
		return ((Long) session.createCriteria(SbiGlWlist.class).setProjection(Projections.rowCount()).add(Restrictions.eq("content.contentId", contentId)).uniqueResult()).intValue();
	
			}
			
		});
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
	public SbiGlWlist loadWlist(SbiGlWlistId id) {
		return load(SbiGlWlist.class, id);
	}

	@Override
	public SbiGlWlistId insertWlist(final SbiGlWlist wlist) {
		return executeOnTransaction(new IExecuteOnTransaction<SbiGlWlistId>() {
			@Override
			public SbiGlWlistId execute(Session session) {

				if (session.get(SbiGlWlist.class, wlist.getId()) != null) {
					// if already exist
					return null;
				}
				return (SbiGlWlistId) session.save(wlist);
			}
		});
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
	public List<SbiGlWlist> listWlistByGlossaryIdAndWordId(Integer glossaryId,
			Integer wordId) {
		return list(new SearchtWlistByGlossaryIdAndWordId(glossaryId, wordId));
	}

//	@Override
//	public SbiGlAttribute loadAttribute(Integer attributeId) {
//		return load(SbiGlAttribute.class, attributeId);
//	}
//
//	@Override
//	public List<SbiGlAttribute> listAttribute() {
//		return list(SbiGlAttribute.class);
//	}
//
//	@Override
//	public List<SbiGlAttribute> listAttributeFiltered(String attribute) {
//		return list(new SearchAttributeByName(attribute));
//	}
//
//	@Override
//	public Integer insertAttribute(SbiGlAttribute attribute) {
//		return (Integer) insert(attribute);
//	}
//
//	@Override
//	public void modifyAttribute(SbiGlAttribute attribute) {
//		update(attribute);
//	}
//
//	@Override
//	public void deleteAttribute(Integer attributeId) {
//		delete(SbiGlAttribute.class, attributeId);
//	}

	@Override
	public List<SbiUdpValue> listWordAttr(Integer wordId) {
		return list(new SearchWordAttrByWordId(wordId));
	}

	@Override
	public Integer insertWordAttr(SbiUdpValue wordAttr) {
		return (Integer) insert(wordAttr);
	}

	@Override
	public void modifyWordAttr(SbiUdpValue wordAttr) {
		update(wordAttr);
	}

	@Override
	public void deleteWordAttr(Integer wordId) {
		delete(SbiUdpValue.class, wordId);
	}

	@Override
	public void deleteWordReferences(Integer id) {

		Session session = null;
		Transaction tx = null;
		LogMF.debug(logger, "IN: id = [{0}]", id);

		try {
			if (id == null) {
				throw new IllegalArgumentException(
						"Input parameter [id] cannot be null");
			}
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				tx = session.beginTransaction();
				Assert.assertNotNull(tx, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException(
						"An error occured while creating the new transaction",
						t);
			}

			Query q = session
					.createQuery("delete from SbiGlReferences where refWord.wordId=:id");
			q.setParameter("id", id);
			q.executeUpdate();
			tx.commit();

		} catch (Throwable t) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDOAException(
					"An unexpected error occured while deleting word references where word id is equal to ["
							+ id + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

	}

	

		@Override
		public List<SbiGlDocWlist> listDocWlist(Integer docwId){

			return list(new SearchListDocWlist(docwId));
		}
		
		@Override
		public SbiGlDocWlist loadDocWlist(SbiGlDocWlistId id){
			return load(SbiGlDocWlist.class, id);
		}
		
		@Override
		public SbiGlDocWlist getDocWlistOrNull(SbiGlDocWlistId id){
			List<SbiGlDocWlist> l = list(new loadDocWlistByDocumentAndWord(id.getDocumentId(),id.getWordId()));
			if (l.isEmpty()) {
				return null;
			} else {
				return l.get(0);
			}
			
		}
		
		@Override
		public SbiGlDocWlistId insertDocWlist(SbiGlDocWlist docwlist){
			return (SbiGlDocWlistId) insert(docwlist);
		}
		
		@Override
		public void deleteDocWlist(SbiGlDocWlistId id){
			delete(SbiGlDocWlist.class, id);
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
 
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public List<SbiGlAttribute> listAttrFromArray(final Object[] arr) {
//		return list(new ICriterion() {
// 
//			@Override
//			public Criteria evaluate(Session session) {
//				Criteria c = session.createCriteria(SbiGlAttribute.class);
//				c.add(Restrictions.in("attributeId", arr));
//				return c;
//			}
//		});
//	}
	
	
	
	@Override
	public List<SbiGlDataSetWlist> listDataSetWlist(Integer datasetId){

		return list(new SearchListDataSetWlist(datasetId));
	}
	
	@Override
	public Map<String, Object> NavigationItem(final JSONObject elem) {

		return executeOnTransaction(new IExecuteOnTransaction<Map<String, Object>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Map<String, Object> execute(Session session) throws JSONException {
				Map<String, Object> map = new HashMap<String, Object>();
				String tmpSearch="";
				Integer tmpPage=null;
				Integer tmp_item_count=null;
				
				String type=elem.getString("type");
				String item="";
				if(elem.has("item"))item=elem.getString("item");
				
				String hql="";
				String countHql="";
				Integer v=null;
				
				//get selected word
				int sizeW=elem.getJSONObject("word").getJSONArray("selected").length();
				String listid="";
				
				for (int i = 0; i < sizeW; i++) {
					listid += elem.getJSONObject("word").getJSONArray("selected").getJSONObject(i).getInt("WORD_ID");
					if (i != sizeW - 1) {
						listid += ",";
					}
				}
				
//#########################################################DOCUMENT############################################################			
							
				if(type.compareTo("all")==0 ||	(item.compareTo("document")==0 && (type.compareTo("search")==0 || type.compareTo("pagination")==0 || type.compareTo("reset")==0 )) ||	( (type.compareTo("click")==0 || type.compareTo("reset")==0)  && item.compareTo("word")==0 )){
				List<SbiObjects> doclist=new ArrayList<SbiObjects>();
			
				tmpSearch=elem.getJSONObject("document").getString("search");
				tmpPage= elem.getJSONObject("document").getInt("page");
				tmp_item_count= elem.getJSONObject("document").getInt("item_number");	
				if(sizeW>0){
					hql= 
							"SELECT  "
							+ "	dw.document.biobjId AS biobjId  ,"
							+ "	dw.document.label AS label  "
							+ "FROM "
							+ "	SbiGlDocWlist dw "
							+ "WHERE "
							+ "	dw.id.wordId IN ("+listid+") "
							+ "	AND dw.document.label LIKE :searchName  "
							+ "GROUP BY "
							+ "	dw.document.biobjId HAVING COUNT(dw.id.wordId) =  "+sizeW;
					
					countHql= "SELECT"
							+ " COUNT(*)  "
							+ "FROM"
							+ " SbiGlDocWlist dw "
							+ "WHERE"
							+ " dw.id.wordId in ("+listid+")"
							+ " AND dw.document.label like :searchName "
							+ "GROUP BY dw.document.biobjId  "
							+ "HAVING COUNT(dw.id.wordId) =  "+sizeW;
					 v=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
					}else{
						hql = "select distinct dw.document.biobjId as biobjId  ,dw.document.label as label "
								+ "FROM SbiGlDocWlist dw where dw.document.label like :searchName";
					 countHql= "select count(distinct dw.document.biobjId) "
								+ "FROM SbiGlDocWlist dw where dw.document.label like :searchName";
					 v=((Long)session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").uniqueResult()).intValue();
				}
				Query q=session.createQuery(hql);
						q.setString("searchName", "%" + tmpSearch + "%");
						q.setResultTransformer(Transformers.aliasToBean(SbiObjects.class));
						
						if(type.compareTo("pagination")==0){
							q.setFirstResult((tmpPage - 1) * tmp_item_count).setMaxResults(tmp_item_count);
						}else{
							q.setFirstResult(0).setMaxResults(tmp_item_count);
						}
						
						doclist=q.list();
				
					map.put("document", (Object)doclist);
					map.put("document_size", (Object)v);
				
						}
				
	//#########################################################DATASET############################################################			
				if(type.compareTo("all")==0 ||	(item.compareTo("dataset")==0 && (type.compareTo("search")==0 || type.compareTo("pagination")==0 || type.compareTo("reset")==0 )) ||	( (type.compareTo("click")==0 || type.compareTo("reset")==0) && item.compareTo("word")==0 )){
				List<Object[]> objlist=new ArrayList<Object[]>();
				List<SbiDataSet> dslist=new ArrayList<SbiDataSet>();
				
			
				tmpSearch=elem.getJSONObject("dataset").getString("search");
				tmpPage= elem.getJSONObject("dataset").getInt("page");
				tmp_item_count= elem.getJSONObject("dataset").getInt("item_number");
				
				if(sizeW>0){
						hql = "SELECT DISTINCT"
								+ " dataset.id.dsId ,"
								+ " dataset.id.organization ,"
								+ " dataset.label "
								+ "FROM "
								+ " SbiDataSet dataset, "
								+ " SbiGlDataSetWlist wl "
								+ "WHERE "
								+ " dataset.id.dsId = wl.id.datasetId "
								+ " AND dataset.id.organization = wl.id.organization "
								+ " AND dataset.active=true"
								+ " AND wl.id.wordId in (" + listid + ")"
								+ " AND dataset.label like :searchName "
								+ "GROUP BY dataset.id.dsId  "
								+ "HAVING COUNT(wl.id.wordId) =  " + sizeW;

						countHql = "SELECT"
								+ " COUNT(*) "
								+ "FROM "
								+ "	SbiDataSet dataset,"
								+ " SbiGlDataSetWlist wl "
								+ "WHERE"
								+ " dataset.id.dsId = wl.id.datasetId "
								+ "	AND dataset.id.organization = wl.id.organization "
								+ "	AND dataset.active=true"
								+ " AND wl.id.wordId in (" + listid + ") "
								+ " AND dataset.label like :searchName  "
								+ "GROUP BY dataset.id.dsId  " + "HAVING "
								+ "	COUNT(wl.id.wordId) =  " + sizeW;

					v=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
					}else{
						hql = "SELECT DISTINCT "
								+ " dataset.id.dsId  ,"
								+ " dataset.id.organization ,"
								+ " dataset.label "
								+ "FROM"
								+ " SbiDataSet dataset,"
								+ " SbiGlDataSetWlist wl "
								+ "WHERE dataset.id.dsId = wl.id.datasetId"
								+ " AND dataset.id.organization = wl.id.organization"
								+ " AND dataset.active=true"
								+ " AND dataset.label like :searchName ";

						countHql = "SELECT "
								+ " COUNT(DISTINCT dataset.id.dsId) "
								+ "FROM "
								+ " SbiDataSet dataset,"
								+ " SbiGlDataSetWlist wl "
								+ "WHERE dataset.id.dsId = wl.id.datasetId"
								+ " AND dataset.id.organization = wl.id.organization"
								+ " AND dataset.active=true"
								+ " AND dataset.label like :searchName ";
					 v=((Long)session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").uniqueResult()).intValue();
				}
				Query q=session.createQuery(hql);
						q.setString("searchName", "%" + tmpSearch + "%");
//						q.setResultTransformer(Transformers.aliasToBean(SbiDataSet.class));
						
						if(type.compareTo("pagination")==0){
							q.setFirstResult((tmpPage - 1) * tmp_item_count).setMaxResults(tmp_item_count);
						}else{
							q.setFirstResult(0).setMaxResults(tmp_item_count);
						}
						
						objlist=q.list();
						for(Object[] o :objlist){
							SbiDataSet tmp=new SbiDataSet();
							tmp.setId(new SbiDataSetId(Integer.parseInt(o[0].toString()),null,(String)o[1]));
							tmp.setLabel(o[2].toString());
							dslist.add(tmp);
						}
				
					map.put("dataset", (Object)dslist);
					map.put("dataset_size", (Object)v);
				
						}		
				
				
//#########################################################BUSINESS CLASS############################################################			
				
				if(type.compareTo("all")==0 ||	(item.compareTo("bness_cls")==0 && (type.compareTo("search")==0 || type.compareTo("pagination")==0 || type.compareTo("reset")==0 )) ||	( (type.compareTo("click")==0 || type.compareTo("reset")==0) && item.compareTo("word")==0 )){
				List<SbiGlBnessCls> bness_cls_list=new ArrayList<SbiGlBnessCls>();
			
				tmpSearch=elem.getJSONObject("bness_cls").getString("search");
				tmpPage= elem.getJSONObject("bness_cls").getInt("page");
				tmp_item_count= elem.getJSONObject("bness_cls").getInt("item_number");	
				if(sizeW>0){
					hql= 
							"SELECT  "
							+ "	dw.bness_cls.bcId AS bcId  ,"
							+ "	dw.bness_cls.label AS label  "
							+ "FROM "
							+ "	SbiGlBnessClsWlist dw "
							+ "WHERE "
							+ "	dw.id.wordId IN ("+listid+") "
							+ "	AND dw.bness_cls.label LIKE :searchName  "
							+ "GROUP BY "
							+ "	dw.bness_cls.bcId HAVING COUNT(dw.id.wordId) =  "+sizeW;
					
					countHql= "SELECT"
							+ " COUNT(*)  "
							+ "FROM"
							+ " SbiGlBnessClsWlist dw "
							+ "WHERE"
							+ " dw.id.wordId in ("+listid+")"
							+ " AND dw.bness_cls.label like :searchName "
							+ "GROUP BY dw.bness_cls.bcId  "
							+ "HAVING COUNT(dw.id.wordId) =  "+sizeW;
					 v=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
					}else{
						hql = "select distinct dw.bness_cls.bcId as bcId  ,dw.bness_cls.label as label "
								+ "FROM SbiGlBnessClsWlist dw where dw.bness_cls.label like :searchName";
					 countHql= "select count(distinct dw.bness_cls.bcId) "
								+ "FROM SbiGlBnessClsWlist dw where dw.bness_cls.label like :searchName";
					 v=((Long)session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").uniqueResult()).intValue();
				}
				Query q=session.createQuery(hql);
						q.setString("searchName", "%" + tmpSearch + "%");
						q.setResultTransformer(Transformers.aliasToBean(SbiGlBnessCls.class));
						
						if(type.compareTo("pagination")==0){
							q.setFirstResult((tmpPage - 1) * tmp_item_count).setMaxResults(tmp_item_count);
						}else{
							q.setFirstResult(0).setMaxResults(tmp_item_count);
						}
						
						bness_cls_list=q.list();
				
					map.put("bness_cls", (Object)bness_cls_list);
					map.put("bness_cls_size", (Object)v);
				
						}
	
				
//#########################################################TABLE############################################################			
				
				if(type.compareTo("all")==0 ||	(item.compareTo("table")==0 && (type.compareTo("search")==0 || type.compareTo("pagination")==0 || type.compareTo("reset")==0 )) ||	( (type.compareTo("click")==0 || type.compareTo("reset")==0) && item.compareTo("word")==0 )){
				List<SbiGlTable> table_list=new ArrayList<SbiGlTable>();
			
				tmpSearch=elem.getJSONObject("table").getString("search");
				tmpPage= elem.getJSONObject("table").getInt("page");
				tmp_item_count= elem.getJSONObject("table").getInt("item_number");	
				if(sizeW>0){
					hql= 
							"SELECT  "
							+ "	dw.table.tableId AS tableId  ,"
							+ "	dw.table.label AS label  "
							+ "FROM "
							+ "	SbiGlTableWlist dw "
							+ "WHERE "
							+ "	dw.id.wordId IN ("+listid+") "
							+ "	AND dw.table.label LIKE :searchName  "
							+ "GROUP BY "
							+ "	dw.table.tableId HAVING COUNT(dw.id.wordId) =  "+sizeW;
					
					countHql= "SELECT"
							+ " COUNT(*)  "
							+ "FROM"
							+ " SbiGlTableWlist dw "
							+ "WHERE"
							+ " dw.id.wordId in ("+listid+")"
							+ " AND dw.table.label like :searchName "
							+ "GROUP BY dw.table.tableId  "
							+ "HAVING COUNT(dw.id.wordId) =  "+sizeW;
					 v=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
					}else{
						hql = "select distinct dw.table.tableId as tableId  ,dw.table.label as label "
								+ "FROM SbiGlTableWlist dw where dw.table.label like :searchName";
					 countHql= "select count(distinct dw.table.tableId) "
								+ "FROM SbiGlTableWlist dw where dw.table.label like :searchName";
					 v=((Long)session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").uniqueResult()).intValue();
				}
				Query q=session.createQuery(hql);
						q.setString("searchName", "%" + tmpSearch + "%");
						q.setResultTransformer(Transformers.aliasToBean(SbiGlTable.class));
						
						if(type.compareTo("pagination")==0){
							q.setFirstResult((tmpPage - 1) * tmp_item_count).setMaxResults(tmp_item_count);
						}else{
							q.setFirstResult(0).setMaxResults(tmp_item_count);
						}
						
						table_list=q.list();
				
					map.put("table", (Object)table_list);
					map.put("table_size", (Object)v);
				
						}
				
	//###########################################################WORD############################################################	
				
				if(type.compareTo("reset")==0 || !((type.compareTo("search")==0 || type.compareTo("pagination")==0) && item.compareTo("word")!=0) && ((item.compareTo("word")==0 && (type.compareTo("search")==0 || type.compareTo("pagination")==0)) || type.compareTo("all")==0 || type.compareTo("click")==0 && item.compareTo("word")!=0 )){
				List<SbiGlWord> wordList=new ArrayList<SbiGlWord>();
				List<SbiGlWord> DocWordList=new ArrayList<SbiGlWord>();
				List<SbiGlWord> DataSetWordList=new ArrayList<SbiGlWord>();
				List<SbiGlWord> BnessClsList=new ArrayList<SbiGlWord>();
				List<SbiGlWord> TableList=new ArrayList<SbiGlWord>();
				v=null;
				int sizeD=elem.getJSONObject("document").getJSONArray("selected").length();
				int sizeDS=elem.getJSONObject("dataset").getJSONArray("selected").length();
				int sizeBC=elem.getJSONObject("bness_cls").getJSONArray("selected").length();
				int sizeTB=elem.getJSONObject("table").getJSONArray("selected").length();
				String listDocid="";
				for (int i = 0; i < sizeD; i++) {
					listDocid += elem.getJSONObject("document").getJSONArray("selected").getJSONObject(i).getInt("DOCUMENT_ID");
					if (i != sizeD - 1) {
						listDocid += ",";
					}
				}
				
				String listDataSetid="";
				for (int i = 0; i < sizeDS; i++) {
					listDataSetid += elem.getJSONObject("dataset").getJSONArray("selected").getJSONObject(i).getInt("DATASET_ID");
					if (i != sizeDS - 1) {
						listDataSetid += ",";
					}
				}
				
				String listBnessClsid="";
				for (int i = 0; i < sizeBC; i++) {
					listBnessClsid += elem.getJSONObject("bness_cls").getJSONArray("selected").getJSONObject(i).getInt("BC_ID");
					if (i != sizeBC - 1) {
						listBnessClsid += ",";
					}
				}
				
				String listTableid="";
				for (int i = 0; i < sizeTB; i++) {
					listTableid += elem.getJSONObject("table").getJSONArray("selected").getJSONObject(i).getInt("TABLE_ID");
					if (i != sizeTB - 1) {
						listTableid += ",";
					}
				}
				
				tmpSearch=elem.getJSONObject("word").getString("search");
				tmpPage= elem.getJSONObject("word").getInt("page");
				tmp_item_count= elem.getJSONObject("word").getInt("item_number");	
				Integer gloId=null;
				String addGloToQuery="  WHERE";
				if(elem.getJSONObject("word").has("GLOSSARY_ID")){
					gloId=elem.getJSONObject("word").getInt("GLOSSARY_ID");
					addGloToQuery=",SbiGlWlist wlc WHERE wlc.id.wordId=sl.word.wordId AND wlc.content.glossaryId="+gloId+" and";
				}
				
				if(sizeD>0){
						hql = "SELECT" + " sl.word.wordId as wordId,"
								+ " sl.word.word as word "
								+ "FROM SbiGlDocWlist sl " + addGloToQuery
								+ " sl.id.documentId in (" + listDocid + ")"
								+ " AND sl.word.word like :searchName "
								+ "GROUP BY sl.word.wordId "
								+ "HAVING count(sl.id.documentId) =  " + sizeD;
						countHql = "SELECT" + " COUNT(*) " + "FROM "
								+ "SbiGlDocWlist sl " + addGloToQuery
								+ " sl.id.documentId in (" + listDocid + ")"
								+ " AND sl.word.word like :searchName  "
								+ "GROUP BY sl.word.wordId "
								+ "HAVING count(sl.id.documentId) =  " + sizeD;
					int tmpv=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
					v=(v==null || tmpv<v) ? tmpv: v;
					Query q=session.createQuery(hql);
						q.setString("searchName", "%" + tmpSearch + "%");
						q.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
						DocWordList=q.list();
				}
				
				if(sizeDS>0){
//					hql= "select sl.word.wordId as wordId  ,sl.word.word as word  FROM SbiDataSet dataset,SbiGlDataSetWlist sl "
//							+ "where dataset.id.dsId = sl.id.datasetId and dataset.id.organization = sl.id.organization and dataset.active=true"
//							+ " and sl.id.datasetId in ("+listDataSetid+") and sl.word.word like :searchName  group by sl.word.wordId  having count(sl.id.datasetId) =  "+sizeDS;
					
					
					hql= "SELECT "
							+ "sl.word.wordId as wordId ,"
							+ "sl.word.word as word "
							+ "FROM "
							+ "SbiDataSet dataset,"
							+ "SbiGlDataSetWlist sl"
							+ addGloToQuery
							+ "  dataset.id.dsId = sl.id.datasetId "
							+ " AND dataset.id.organization = sl.id.organization "
							+ " AND dataset.active=true"
							+ " AND sl.id.datasetId in ("+listDataSetid+")"
							+ " AND sl.word.word like :searchName "
							+ "GROUP BY sl.word.wordId  "
							+ "HAVING COUNT(sl.id.datasetId) =  "+sizeDS;
					
					countHql= "SELECT"
							+ " COUNT(*)"
							+ "FROM "
							+ " SbiDataSet dataset,"
							+ " SbiGlDataSetWlist sl "
							+ addGloToQuery
							+" dataset.id.dsId = sl.id.datasetId"
							+ " AND dataset.id.organization = sl.id.organization"
							+ " AND dataset.active=true"
							+ " AND sl.id.datasetId in ("+listDataSetid+")"
							+ " AND sl.word.word like :searchName  "
							+ "GROUP BY sl.word.wordId "
							+ "HAVING COUNT(sl.id.datasetId) =  "+sizeDS;
					 int tmpv=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
					 v=(v==null || tmpv<v) ? tmpv: v;
					 Query q=session.createQuery(hql);
						q.setString("searchName", "%" + tmpSearch + "%");
						q.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
						DataSetWordList=q.list();
				}
				
				
				if(sizeBC>0){
					hql = "SELECT" + " sl.word.wordId as wordId,"
							+ " sl.word.word as word "
							+ "FROM SbiGlBnessClsWlist sl " + addGloToQuery
							+ " sl.id.bcId in (" + listBnessClsid + ")"
							+ " AND sl.word.word like :searchName "
							+ "GROUP BY sl.word.wordId "
							+ "HAVING count(sl.id.bcId) =  " + sizeBC;
					countHql = "SELECT" + " COUNT(*) " + "FROM "
							+ "SbiGlBnessClsWlist sl " + addGloToQuery
							+ " sl.id.bcId in (" + listBnessClsid + ")"
							+ " AND sl.word.word like :searchName  "
							+ "GROUP BY sl.word.wordId "
							+ "HAVING count(sl.id.bcId) =  " + sizeBC;
				int tmpv=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
				v=(v==null || tmpv<v) ? tmpv: v;
				Query q=session.createQuery(hql);
					q.setString("searchName", "%" + tmpSearch + "%");
					q.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
					BnessClsList=q.list();
			}
				
				if(sizeTB>0){
					hql = "SELECT" + " sl.word.wordId as wordId,"
							+ " sl.word.word as word "
							+ "FROM SbiGlTableWlist sl " + addGloToQuery
							+ " sl.id.tableId in (" + listTableid + ")"
							+ " AND sl.word.word like :searchName "
							+ "GROUP BY sl.word.wordId "
							+ "HAVING count(sl.id.tableId) =  " + sizeTB;
					countHql = "SELECT" + " COUNT(*) " + "FROM "
							+ "SbiGlTableWlist sl " + addGloToQuery
							+ " sl.id.tableId in (" + listTableid + ")"
							+ " AND sl.word.word like :searchName  "
							+ "GROUP BY sl.word.wordId "
							+ "HAVING count(sl.id.tableId) =  " + sizeTB;
				int tmpv=session.createQuery(countHql).setString("searchName", "%" + tmpSearch + "%").list().size();
				v=(v==null || tmpv<v) ? tmpv: v;
				Query q=session.createQuery(hql);
					q.setString("searchName", "%" + tmpSearch + "%");
					q.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
					TableList=q.list();
			}
				
				if(sizeD==0 && sizeDS==0 && sizeBC==0 && sizeTB==0){
					wordList=listWordFiltered(tmpSearch,tmpPage,tmp_item_count,gloId);
					v=wordCount(tmpSearch ,gloId);
				}else{
					
					Set<SbiGlWord> wordSet = new HashSet<SbiGlWord>();
					wordSet.addAll(DataSetWordList);
					wordSet.addAll(DocWordList);
					wordSet.addAll(BnessClsList);
					wordSet.addAll(TableList);
					

					if(!DataSetWordList.isEmpty()|| sizeDS>0)wordSet.retainAll(DataSetWordList);
					if(!DocWordList.isEmpty()|| sizeD>0)wordSet.retainAll(DocWordList);
					if(!BnessClsList.isEmpty()|| sizeBC>0)wordSet.retainAll(BnessClsList);
					if(!TableList.isEmpty()|| sizeTB>0)wordSet.retainAll(TableList);
					
					wordList.addAll(wordSet);
					
					//faccio una paginazione dei risultati spezzando l'array
					int endV=((tmpPage - 1) * tmp_item_count)+tmp_item_count;
					endV=endV>wordList.size()? wordList.size():endV;
					if(type.compareTo("pagination")==0){
						wordList=wordList.subList(((tmpPage - 1) * tmp_item_count),endV );
					}else{
					wordList=wordList.subList(0, endV);	
					}
				}
				map.put("word", (Object)wordList);
				map.put("word_size", (Object)v);
				
			}
				
				return map;
			}
		});

	}
	
	@Override
	public SbiGlBnessCls loadBnessCls(Integer bcId) {
		return load(SbiGlBnessCls.class, bcId);
	}
	
	@Override
	public SbiGlTable loadTable(Integer tableId) {
		return load(SbiGlTable.class, tableId);
	}
	
}
