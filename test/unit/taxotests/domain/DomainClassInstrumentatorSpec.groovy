package taxotests.domain

import taxotests.service.SearchService
import taxotests.service.SpecificationSupport
import taxotests.service.TaggingService
import taxotests.testobjects.TestDomainClass1

/**
 * lmuniz (9/15/12 8:30 PM)
 */
class DomainClassInstrumentatorSpec extends SpecificationSupport {
    def mockTaggingService
    def mockSearchService

    def o

    def setup() {
        mockTaggingService = Mock(TaggingService)
        mockSearchService = Mock(SearchService)

        DomainClassInstrumentator domainClassInstrumentator = new DomainClassInstrumentator(
            taggingService: mockTaggingService,
            searchService: mockSearchService
        )
        domainClassInstrumentator.instrumentDomainClasses(TestDomainClass1)

        o = new TestDomainClass1()
    }

    def 'tagWithLocale MetaMethod is created in a Domain class'() {
        when:
        o.tagWithLocale(SPAIN, [T1])

        then:
        1 * mockTaggingService.tagAndTranslate(o, SPAIN, [T1])
        0 * _._ // no (more) method call on any mock
    }

    def 'findAllByTag MetaMethod is created in a Domain class'() {
        when:
        TestDomainClass1.findAllByTag(SPAIN, T1)

        then:
        1 * mockSearchService.findAllByTag(TestDomainClass1, SPAIN, T1)
        0 * _._ // no (more) method call on any mock
    }

    def 'findAllByTags MetaMethod is created in a Domain class'() {
        when:
        TestDomainClass1.findAllByTags(SPAIN, [T1])

        then:
        1 * mockSearchService.findAllByTagDisjunction(TestDomainClass1, SPAIN, [T1])
        0 * _._ // no (more) method call on any mock
    }
}
