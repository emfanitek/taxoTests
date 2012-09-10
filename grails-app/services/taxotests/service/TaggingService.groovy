package taxotests.service

import taxotests.TagReference

class TaggingService {

    def tag(obj, Locale locale, String tag) {
        def referenceTag = TagReference.findOrSaveWhere(locale: locale.toString(), tag: tag)

        def taxo = ['by_locale', locale.toString(), tag]
        obj.addToTaxonomy(taxo)
        referenceTag.addToTaxonomy(taxo)
    }
}
