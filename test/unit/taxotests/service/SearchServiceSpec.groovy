package taxotests.service

import static java.util.Locale.FRANCE
import static java.util.Locale.UK
import taxotests.testobjects.TestDomainClass2
import taxotests.testobjects.TestDomainClass1

class SearchServiceSpec extends SpecificationSupport {
    SearchService searchService
    def taggingService

    def setup() {
        new Initializer().initialize(
            ['searchService', 'taggingService'],
            this
        )
    }

    def 'you can find any tagged object by searching by tag'() {
        setup:
        taggingService.tag(o1, locale, tag1)

        when:
        Collection foundObjectsOfClass1ForTag1 = searchService.findAllByExactTag(o1.getClass(), locale, tag1)

        then:
        setsAreEqual([o1], foundObjectsOfClass1ForTag1)

        where:
        locale | tag1
        UK     | T1
        FRANCE | T2
    }

    //Bug in taxonomy plugin
    //see http://jira.grails.org/browse/GPTAXONOMY-6
    def 'you will not find an untagged object'() {
        expect:
        searchService.findAllByExactTag(o1.getClass(), UK, 'randomTag').empty

/*
        expect:
        searchService.findAllByExactTag(o1.getClass(), UK, 'randomTag').empty
        then:
        thrown(NullPointerException)
*/
    }

    def 'you will only find matching tagged objects of same class by searching by tag'() {
        setup:
        def o3 = new TestDomainClass2(name: 'c2_o3').save()
        def o4 = new TestDomainClass2(name: 'c2_o4').save()
        taggingService.tag(o1, locale, tag1)
        taggingService.tag(o2, locale, tag2)
        taggingService.tag(o3, locale, tag1)
        taggingService.tag(o3, locale, tag2)
        taggingService.tag(o4, locale, tag2)

        def class1 = o1.getClass()
        def class2 = o3.getClass()

        when:
        Collection foundObjectsOfClass1ForTag1 = searchService.findAllByExactTag(class1, locale, tag1)
        Collection foundObjectsOfClass1ForTag2 = searchService.findAllByExactTag(class1, locale, tag2)
        Collection foundObjectsOfClass2ForTag1 = searchService.findAllByExactTag(class2, locale, tag1)
        Collection foundObjectsOfClass2ForTag2 = searchService.findAllByExactTag(class2, locale, tag2)

        then:
        setsAreEqual([o1], foundObjectsOfClass1ForTag1)
        setsAreEqual([o2], foundObjectsOfClass1ForTag2)
        setsAreEqual([o3], foundObjectsOfClass2ForTag1)
        setsAreEqual([o3, o4], foundObjectsOfClass2ForTag2)

        where:
        locale | tag1 | tag2
        UK     | T1   | T2
        FRANCE | T2   | T1
    }

    def "you will find an object whose tags have been translated, by searching for a tag's translation"() {
        setup:
        taggingService.tagAndTranslate(o1, srcLocale, [t1])

        when:
        Collection foundObjects = searchService.findAllByExactTag(o1.getClass(), dstLocale, t1_trans)

        then:
        setsAreEqual(foundObjects, [o1])

        where:
        t1      | t1_trans | srcLocale | dstLocale
        'Hello' | 'Salut'  | UK        | FRANCE
    }

    def 'you will find several objects by a translated tag'() {
        setup:
        [o1, o2].each {o ->
            taggingService.tagAndTranslate(o, locale, [tag])
        }
        def o3 = new TestDomainClass1(name: 'c1_o3').save()
        taggingService.tagAndTranslate(o3, locale, [irrelevantTag])

        when:
        Collection allObjs = searchService.findAllByExactTag(o1.getClass(), translationLocale, translatedTag)
        assert allObjs.size() == 2

        then:
        setsAreEqual([o1, o2], allObjs)

        where:
        locale | tag     | translationLocale | translatedTag | irrelevantTag
        UK     | 'Hello' | FRANCE            | 'Salut'       | 'World'
        UK     | 'Hello' | SPAIN             | 'Hola'        | 'World'
        UK     | 'World' | SPAIN             | 'Mundo'       | 'Hello'
    }


}
