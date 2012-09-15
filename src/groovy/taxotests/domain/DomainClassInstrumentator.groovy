package taxotests.domain

import taxotests.service.TaggingService
import taxotests.service.SearchService
import taxotests.service.TagTranslationService

/**
 * lmuniz (9/15/12 3:06 PM)
 */
class DomainClassInstrumentator {
    TaggingService taggingService
    SearchService searchService

    void instrumentDomainClasses(Class domainClass) {
        domainClass.metaClass.'static'.findAllByTag = {Locale locale, String tag ->
            searchService.findAllByTag(domainClass, locale, tag)
        }

        domainClass.metaClass.'static'.findAllByTags = {Locale locale, Collection<String> tags ->
            searchService.findAllByTagDisjunction(domainClass, locale, tags)
        }

        domainClass.metaClass.tagWithLocale = {Locale locale, Collection<String> tags ->
            taggingService.tagAndTranslate(delegate, locale, tags)
        }
    }
}
