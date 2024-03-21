# 🌱 SpongoUnit 
SpongoUnit is a data driven integration test utility for MongoDB + Spring Boot + Spock.

**Development status: Alpha.** Unexpected behaviour can occur. Additional tests are recommended.

## Features

- ✅ Seeding and comparing database with provided datasets.
- ✅ Automatic database credentials retrieval from Spring context.
- ✅ Spring Boot Testcontainers support.
- ✅ Multiple collections support.
- ✅ Support of multiple types from `Int64` to `Binary`, `JavaScript`, and `RegExp`.

## Getting started

Declare datasets in the resource classpath. Example:

`resources/user/user.json`
```json
[
  {
    "collectionName": "user",
    "documents": [
      {
        "_id": {
          "$$OBJECT_ID": "5db7545b7b615c739732c777"
        },
        "name": "Bob",
        "created": {
          "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
        }
      }
    ]
  }
]
```

`resources/user/modifiedUser.json`
```json
[
  {
    "collectionName": "user",
    "documents": [
      {
        "_id": {
          "$$OBJECT_ID": "5db7545b7b615c739732c777"
        },
        "name": "Mark",
        "created": {
          "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
        }
      }
    ]
  }
]
```


Declare `@SeedDataset` or `@ExpectedDataset` annotation on your test method or class.

```groovy
@DataMongoTest
class UserSpec extends Specification {
    
    @Autowired
    UserRepository userRepository

    @SeedDataset("user.json")
    @ExpectedDataset("modifiedUser.json")
    def "test"() {
        given: "Typical user"
        def userId = ObjectId("5db7545b7b615c739732c777")
        def newName = "Mark"
        
        when: "User is modified"
        def user = userRepository.findById(userId)
        user.setName(newName)
        userRepository.save(user)

        then: "No exception is thrown and database set is correct"
        noExceptionThrown()
    }

}

```



## Roadmap

- Support of timeseries collections.
- Combining class-level and method-level annotation datasets.

## Attribution & reference

Project is a Spock-adapted fork of [MongoUnit]([https://github.com/mongounit/mongounit]) that is intended to work with JUnit.
Special thanks to [Yaakov Chaikin](https://github.com/ychaikin).