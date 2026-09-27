"""Verify the lab's resource contract without Android Studio or third-party modules."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
ANDROID = "{http://schemas.android.com/apk/res/android}"
EXPECTED = {
    "layout": ("phone_portrait", "vertical"),
    "layout-land": ("phone_landscape", "horizontal"),
    "layout-sw600dp": ("tablet_sw600dp", "horizontal"),
}
REQUIRED_IDS = {
    "main", "lab_title", "layout_title", "words", "word_this", "word_is",
    "word_my", "word_first", "application_title", "actions", "change_button", "cancel_button",
}


def verify():
    variants = {p.parent.name for p in RES.glob("layout*/activity_main.xml")}
    assert variants == set(EXPECTED), f"Unexpected layout variants: {variants}"
    for folder, (tag, orientation) in EXPECTED.items():
        tree = ET.parse(RES / folder / "activity_main.xml").getroot()
        assert tree.get(ANDROID + "tag") == tag, folder
        assert tree.get(ANDROID + "orientation") == orientation, folder
        ids = [e.get(ANDROID + "id").split("/")[-1] for e in tree.iter()
               if e.get(ANDROID + "id")]
        assert len(ids) == len(set(ids)), f"Duplicate IDs in {folder}"
        assert set(ids) == REQUIRED_IDS, f"Missing or unexpected controls in {folder}"
        for node in tree.iter():
            for attribute, value in node.attrib.items():
                assert not value.endswith("px"), f"Pixel dimension: {folder}: {attribute}"
            for axis in ("width", "height"):
                value = node.get(ANDROID + "layout_" + axis)
                if value == "0dp":
                    assert float(node.get(ANDROID + "layout_weight", "0")) > 0, folder
        print(f"PASS {folder}: IDs, orientation, tag, responsive dimensions")
    manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
    activity = manifest.find("application/activity")
    assert activity.get(ANDROID + "screenOrientation") is None, "Rotation must not be locked"
    assert activity.get(ANDROID + "configChanges") is None, "Let Android recreate on rotation"
    source = (ROOT / "app/src/main/java/com/example/practical2/MainActivity.java").read_text()
    assert "setContentView(R.layout.activity_main)" in source
    print("PASS automatic Android resource selection and unrestricted rotation")


if __name__ == "__main__":
    verify()
