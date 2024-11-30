import com.friska.kompakt.JSONObject;
import com.friska.kompakt.JSONParser;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.friska.kompakt.JSONParser.*;
import static com.friska.kompakt.NumberType.*;

/**
 * This class tests that serialisation composed with deserialisation and vice-versa is the identity function.
 * We decided that testing trivial cases is redundant, as these tests cases are already covered by the other test
 * classes, hence, this class will immediately resort to complex JSONs. For simplicity, the type of each JSON string will
 * be object.
 */
public class IdentityTest {

    @Test
    public void testMedium(){

        String s1 = """
                {
                  "Hello": {
                    "array": [
                      1,2,3,4,5,null
                    ]
                  }
                }
                """;

        String s2 = """
                {
                   "Hello": {
                     "array": [
                       1,2,3,4,5,null, {
                         "Hello": "\\n\\n\\n\\t\\t\\t"
                       },
                       1.23
                     ],
                     "obj": {
                 
                     }
                   }
                 }
                """;
        String s3 = """
                {
                  "array": [
                   2,"hi"
                  ]
                }
                """;
        String s4 = """
                {
                  "array": [
                   2,"hi",[]
                  ]
                }
                """;

        String s5 = "{\"\\\"\": \"Hi\"}";

        testBigDecimal(s1, s2, s3, s4, s5);
    }

    @Test
    public void testContrived(){
        String c1 = """
                {
                  "Hello": {
                    "array": [
                      1,2,3,4,5,null, {
                        "Hello": "\\n\\n\\n\\t\\t\\t"
                      }, 1.23],
                    "obj": {
                      "contrived array": [
                        [[],[],[[[[],[[[],[],3,[],"null",[],null,{"contrived":  [
                          [[],[],[[[[],[[[],[],3,[],"null",[],null,{"contrived":  true}]]]]]],
                          [[[[],[],[[[[],[[[],[],3,[],"null",[],null,{"contrived":  true}]]]]]],
                            [], [[]], []]], [], []]}]]]]]],
                        [],
                        [],
                        []
                      ]
                    }
                  }
                }
                """;


        String c2 = """
                {
                  "Root": {
                    "Level-1": [
                      true,
                      false,
                      null,
                      [
                        1,
                        2,
                        "infinity",
                        [
                          [],
                          [{}],
                          [
                            [
                              {"key": "value"},
                              [null, {}, [[]]],
                              {"nested": [[[], {"deeper": "😵‍💫"}, 42]]}
                            ]
                          ]
                        ]
                      ],
                      {
                        "Meta": {
                          "name": "JSONception",
                          "description": {
                            "phrases": ["This is fine.", "Everything is broken."],
                            "inception": {
                              "yes?": [[[[[[[[[]]]]]]], "No"]]
                            }
                          }
                        }
                      }
                    ],
                    "Spaces and \\"quotes\\"": "\\t\\t\\t\\n\\n",
                    "Nested Arrays": [
                      [
                        [
                          {
                            "🤯": {
                              "nothing": [[[]]],
                              "everything": [
                                [
                                  [
                                    [{"what is happening": "even deeper?"}],
                                    [
                                      {"Contrived": "Maximum effort"},
                                      {
                                        "Circular?": [
                                          [
                                            {
                                              "Arrayception": [
                                                "Let",
                                                "me",
                                                "out!",
                                                { "key?": "value" }
                                              ]
                                            }
                                          ]
                                        ]
                                      }
                                    ]
                                  ]
                                ]
                              ]
                            }
                          }
                        ]
                      ]
                    ],
                    "Contrived Keys": {
                      "empty": {},
                      "odd": { "0x01": { "nested?" : ["yes", [1.23456789e12345], null] } },
                      "😹": [
                        [
                          [[]],
                          [[[], [[]], "null"]],
                          [[], { "undefined": [[true, false]] }],
                          {"deeper still": {"levels": { "4": [[[[]], "maybe?", null]] }}}
                        ]
                      ]
                    },
                    "Callbacks": {
                      "success": {
                      },
                "failure": {
                "error?": { "catch": "42" },
                "messages": ["try again?", ["or not"], 0, -9999]
                }
                },
                "Numbers": [0.1, 0.0000001, -99999999999999999],
                "Mega Nulls": [[[[[null, null, {"also": null}]]]], {}, []],
                "Layer Cake": [
                {
                "layers": [
                {"thin": []},
                {"medium": [{"a": [[[]], "bottom"], "b": []}]},
                {"thick": [{"bottomless": [[[[[[]]]]]]}]}
                ]
                }
                ],
                "End": null
                }
                }
                            
                """;

        String c3 = """
                {
                  "🌌": {
                    "✨magic_number✨": 42,
                    "💾_save_game": true,
                    "🌈color_palette": ["#FF00FF", "#00FFFF", "#FFFF00"],
                    "🧪": {
                      "⚗️_potion_strength": 9999,
                      "🍄_mushroom_count": 3.14159,
                      "🍷_wine_quality": "divine"
                    }
                  },
                  "🚀_space_mission": {
                    "🛸crew": [
                      {"👩‍🚀name": "Commander Starbeam", "🛠️_role": "Pilot"},
                      {"👨‍🚀name": "Dr. Nebula", "🧬_role": "Scientist"},
                      {"🤖name": "X-4200", "⚡role": "Mechanic"}
                    ],
                    "🌍_destination": "Proxima Centauri b",
                    "⏱️time_remaining": "42 light years 🚶"
                  },
                  "🐉": {
                    "🔥dragon_fire": true,
                    "💰treasure_value": "∞",
                    "🧚‍♂️fairy_friends": ["Tinkerbell", "Pixie", "Lumos"]
                  },
                  "🎲_rng_simulation": {
                    "🃏": "Joker",
                    "🎯lucky_number": 7,
                    "🎲rolls": [6, 6, 6, 1]
                  },
                  "🪐weird_astronomy_data": {
                    "🌖half_moon_visibility": "visible",
                    "🌌galactic_overlord": {
                      "👑name": "Xar'thul",
                      "👾power_level": "Over 9000"
                    },
                    "📡radio_signals_received": [
                      {"📡": "💬HELLO", "🕒": "2024-11-30T12:00:00Z"},
                      {"📡": "🛑STOP", "🕒": "2024-11-30T12:01:00Z"}
                    ]
                  },
                  "💀☠️": null,
                  "🙃": "This is absolutely pointless.",
                  "🍕toppings": [
                    "🍍",
                    "🍕",
                    "🍖",
                    "👽 (alien fungus)"
                  ]
                }
                                
                """;

        testBigDecimal(c1, c2, c3);
    }

    private void testBigDecimal(String... jsons){
        for (String json : jsons) {
            JSONObject exp1 = parseAsObject(json, BIGDECIMAL);
            assertEquals(parseAsObject(exp1.serialise(), BIGDECIMAL), exp1);
        }
    }

}
