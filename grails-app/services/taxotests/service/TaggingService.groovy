package taxotests.service

import taxotests.TagReference
import com.grailsrocks.taxonomy.Taxon

class TaggingService {
    def tagTranslationService

    void tag(obj, Locale locale, String tag) {
        def referenceTag = tagTranslationService.findTagReference(locale, tag)

        if (referenceTag == null) {
            def taxo = ['by_locale', locale.toString(), tag]
            referenceTag = new TagReference(locale: locale.toString(), tag: tag).save()
            referenceTag.addToTaxonomy(taxo)
            obj.addToTaxonomy(taxo)
        } else {
            referenceTag.taxonomies.each {taxo ->
                obj.addToTaxonomy(taxo)
            }
        }
    }

    void tagAndTranslate(obj, Locale locale, List<String> tags) {
        tags.each {tag(obj, locale, it)}
        tagTranslationService.translateAllTags(obj)
    }

    Collection<String> getAllTags(obj, Locale locale) {
        Closure matchingLocale = this.&matchesLocale.rcurry(locale.toString())
        getAllTagsInTaxonList(obj.taxonomies.findAll(matchingLocale))
    }

    Collection<String> getAllTags(obj) {
        getAllTagsInTaxonList(obj.taxonomies)
    }

    private Collection<String> getAllTagsInTaxonList(Collection<Taxon> taxons) {
        taxons.collect {Taxon t -> t.name}
    }

    private boolean matchesLocale(Taxon t, String localeAsString) {
        t.parent.name == localeAsString
    }
}
