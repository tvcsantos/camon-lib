/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.bibtex;

import bibtex.dom.BibtexAbstractEntry;
import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexConcatenatedValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexMacroDefinition;
import bibtex.dom.BibtexMacroReference;
import bibtex.dom.BibtexMultipleValues;
import bibtex.dom.BibtexNode;
import bibtex.dom.BibtexPerson;
import bibtex.dom.BibtexPersonList;
import bibtex.dom.BibtexPreamble;
import bibtex.dom.BibtexString;
import bibtex.dom.BibtexToplevelComment;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author tvcsantos
 */
public class BibtexUtilities {
    private BibtexUtilities() {
        throw new UnsupportedOperationException();
    }

    public static <T extends BibtexNode> T clone(BibtexFile owner, T node) 
            throws CloneNotSupportedException {
        if (node instanceof BibtexFile) {
            BibtexFile bibtexFile = (BibtexFile) node;
            List<BibtexAbstractEntry> entries = bibtexFile.getEntries();
            for (BibtexAbstractEntry entry : entries) {
                owner.addEntry(clone(owner, entry));
            }
            return (T)owner;
        } else if (node instanceof BibtexEntry) {
            BibtexEntry bibtexEntry = (BibtexEntry) node;
            Map<String, BibtexAbstractValue> fields =
                    bibtexEntry.getFields();
            BibtexEntry newEntry = owner.makeEntry(
                    bibtexEntry.getEntryType(), bibtexEntry.getEntryKey());
            for (Entry<String, BibtexAbstractValue> entry : fields.entrySet()) {
                String field = entry.getKey();
                BibtexAbstractValue value = entry.getValue();
                newEntry.addFieldValue(field, clone(owner, value));
            }
            return (T)newEntry;
        } else if (node instanceof BibtexToplevelComment) {
            BibtexToplevelComment bibtexToplevelComment = 
                    (BibtexToplevelComment) node;
            String content = bibtexToplevelComment.getContent();
            BibtexToplevelComment makeToplevelComment =
                    owner.makeToplevelComment(content);
            return (T)makeToplevelComment;
        } else if (node instanceof BibtexPreamble) {
            BibtexPreamble bibtexPreamble = (BibtexPreamble) node;
            BibtexAbstractValue content = bibtexPreamble.getContent();
            BibtexPreamble makePreamble = owner.makePreamble(clone(owner, content));
            return (T)makePreamble;
        } else if (node instanceof BibtexMacroDefinition) {
            BibtexMacroDefinition bibtexMacroDefinition =
                    (BibtexMacroDefinition) node;
            String key = bibtexMacroDefinition.getKey();
            BibtexAbstractValue value = bibtexMacroDefinition.getValue();
            BibtexMacroDefinition makeMacroDefinition = 
                    owner.makeMacroDefinition(key, clone(owner, value));
            return (T)makeMacroDefinition;
        } else if (node instanceof BibtexPerson) {
            BibtexPerson bibtexPerson = (BibtexPerson) node;
            String first = bibtexPerson.getFirst();
            String last = bibtexPerson.getLast();
            String lineage = bibtexPerson.getLineage();
            String preLast = bibtexPerson.getPreLast();
            boolean others = bibtexPerson.isOthers();
            BibtexPerson makePerson = 
                    owner.makePerson(first, preLast, last, lineage, others);
            return (T)makePerson;
        } else if (node instanceof BibtexConcatenatedValue) {
            BibtexConcatenatedValue bibtexConcatenatedValue =
                    (BibtexConcatenatedValue) node;
            BibtexAbstractValue left = bibtexConcatenatedValue.getLeft();
            BibtexAbstractValue right = bibtexConcatenatedValue.getRight();
            BibtexConcatenatedValue makeConcatenatedValue =
                    owner.makeConcatenatedValue(clone(owner, left),
                    clone(owner, right));
            return (T)makeConcatenatedValue;
        } else if (node instanceof BibtexString) {
            BibtexString bibtexString = (BibtexString) node;
            String content = bibtexString.getContent();
            BibtexString makeString = owner.makeString(content);
            return (T)makeString;
        } else if (node instanceof BibtexMacroReference) {
            BibtexMacroReference bibtexMacroReference =
                    (BibtexMacroReference) node;
            String key = bibtexMacroReference.getKey();
            BibtexMacroReference makeMacroReference =
                    owner.makeMacroReference(key);
            return (T)makeMacroReference;
        } else if (node instanceof BibtexPersonList) {
            BibtexPersonList bibtexPersonList = (BibtexPersonList) node;
            List<BibtexPerson> list = bibtexPersonList.getList();
            BibtexPersonList makePersonList = owner.makePersonList();
            for (BibtexPerson person : list) {
                makePersonList.add(clone(owner, person));
            }
            return (T)makePersonList;
        } else if (node instanceof BibtexMultipleValues) {
            BibtexMultipleValues bibtexMultipleValues =
                    (BibtexMultipleValues) node;
            List<BibtexAbstractValue> values =
                    bibtexMultipleValues.getValues();
            BibtexMultipleValues makeBibtexMultipleValues =
                    owner.makeBibtexMultipleValues();
            for (BibtexAbstractValue value : values) {
                makeBibtexMultipleValues.addValue(clone(owner, value));
            }
            return (T)makeBibtexMultipleValues;
        }
        throw new CloneNotSupportedException();
    }
}
