
/* First created by JCasGen Mon Feb 05 09:56:28 CET 2018 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** The reference to an external resource
 * Updated by JCasGen Mon Feb 05 09:56:28 CET 2018
 * @generated */
public class ResourceEntry_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ResourceEntry.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ResourceEntry");
 
  /** @generated */
  final Feature casFeat_source;
  /** @generated */
  final int     casFeatCode_source;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSource(int addr) {
        if (featOkTst && casFeat_source == null)
      jcas.throwFeatMissing("source", "de.julielab.jcore.types.ResourceEntry");
    return ll_cas.ll_getStringValue(addr, casFeatCode_source);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSource(int addr, String v) {
        if (featOkTst && casFeat_source == null)
      jcas.throwFeatMissing("source", "de.julielab.jcore.types.ResourceEntry");
    ll_cas.ll_setStringValue(addr, casFeatCode_source, v);}
    
  
 
  /** @generated */
  final Feature casFeat_entryId;
  /** @generated */
  final int     casFeatCode_entryId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getEntryId(int addr) {
        if (featOkTst && casFeat_entryId == null)
      jcas.throwFeatMissing("entryId", "de.julielab.jcore.types.ResourceEntry");
    return ll_cas.ll_getStringValue(addr, casFeatCode_entryId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setEntryId(int addr, String v) {
        if (featOkTst && casFeat_entryId == null)
      jcas.throwFeatMissing("entryId", "de.julielab.jcore.types.ResourceEntry");
    ll_cas.ll_setStringValue(addr, casFeatCode_entryId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_version;
  /** @generated */
  final int     casFeatCode_version;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getVersion(int addr) {
        if (featOkTst && casFeat_version == null)
      jcas.throwFeatMissing("version", "de.julielab.jcore.types.ResourceEntry");
    return ll_cas.ll_getStringValue(addr, casFeatCode_version);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setVersion(int addr, String v) {
        if (featOkTst && casFeat_version == null)
      jcas.throwFeatMissing("version", "de.julielab.jcore.types.ResourceEntry");
    ll_cas.ll_setStringValue(addr, casFeatCode_version, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ResourceEntry_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_source = jcas.getRequiredFeatureDE(casType, "source", "uima.cas.String", featOkTst);
    casFeatCode_source  = (null == casFeat_source) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_source).getCode();

 
    casFeat_entryId = jcas.getRequiredFeatureDE(casType, "entryId", "uima.cas.String", featOkTst);
    casFeatCode_entryId  = (null == casFeat_entryId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_entryId).getCode();

 
    casFeat_version = jcas.getRequiredFeatureDE(casType, "version", "uima.cas.String", featOkTst);
    casFeatCode_version  = (null == casFeat_version) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_version).getCode();

  }
}



    