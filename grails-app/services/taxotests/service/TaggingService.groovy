package taxotests.service

import taxotests.TagReference

class TaggingService {
    def tagTranslationService

    def tag(obj, Locale locale, String tag) {
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
}
