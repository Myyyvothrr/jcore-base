
/* First created by JCasGen Mon Feb 05 09:56:22 CET 2018 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** Accumulative type for all other types of publications
 * Updated by JCasGen Mon Feb 05 09:56:22 CET 2018
 * @generated */
public class OtherPub_Type extends PubType_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = OtherPub.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.OtherPub");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public OtherPub_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    