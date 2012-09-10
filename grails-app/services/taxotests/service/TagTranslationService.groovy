package taxotests.service

import taxotests.TagReference
import com.grailsrocks.taxonomy.Taxon

class TagTranslationService {
    def availableLocales
    def translationService
    def taxonomyService
    static transactional = true

    void translateTag(obj, Locale referenceLocale, String referenceTag) {
        TagReference tagRef = TagReference.findByLocaleAndTag(referenceLocale.toString(), referenceTag)
        translateTag(tagRef)
        tagRef.taxonomies.each {Taxon t ->
            String tag = t.name
            String locale = t.parent.name
            obj.addToTaxonomy(['by_locale', locale, tag])
        }
    }

    void translateAllTags(obj) {
        obj.taxonomies.each {Taxon t ->
            def tagRef = findTagReference(t)
            translateTag(obj, tagRef.localeObj, tagRef.tag)
        }
    }

    private void translateTag(TagReference referenceTag) {
        def existingTranslations = referenceTag.taxonomies
        def translatedLocales = findTranslatedLocales(existingTranslations)
        def targetLocales = findTargetLocales(translatedLocales)
        targetLocales.each {Locale target ->
            def translated = translationService.translate(referenceTag.tag, referenceTag.localeObj, target)
            referenceTag.addToTaxonomy(['by_locale', target.toString(), translated])
        }
    }

    private TagReference findTagReference(Taxon taxon) {
        List relatedTagReferences = taxonomyService.findObjectsByTaxon(TagReference, ['by_locale', taxon.parent.name, taxon.name ])
        assert relatedTagReferences.size() == 1
        relatedTagReferences[0]
    }

    private Set<String> findTranslatedLocales(existingTranslations) {
        existingTranslations.collect {Taxon t -> t.parent.name} as Set
    }

    private Collection findTargetLocales(translatedLocales) {
        availableLocales.findAll {Locale l ->
            !(l.toString() in translatedLocales)
        }
    }
}
