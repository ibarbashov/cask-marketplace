/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.marketplace;

import co.cask.marketplace.spec.PackageMeta;
import co.cask.marketplace.spec.PackageSpec;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Contains all files in a package.
 */
public class Package {
  private final String name;
  private final String version;
  private final PackageMeta meta;
  @Nullable
  private final File license;
  @Nullable
  private final File icon;
  private final SignedFile spec;
  private final SignedFile archive;
  // all the files that are in the package, plus their signatures.
  private final List<SignedFile> files;

  public Package(String name, String version, PackageMeta meta, File license, File icon,
                 SignedFile archive, SignedFile spec, List<SignedFile> files) {
    this.name = name;
    this.version = version;
    this.meta = meta;
    this.archive = archive;
    this.spec = spec;
    this.license = license;
    this.icon = icon;
    this.files = Collections.unmodifiableList(files);
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public PackageMeta getMeta() {
    return meta;
  }

  public SignedFile getArchive() {
    return archive;
  }

  public SignedFile getSpec() {
    return spec;
  }

  @Nullable
  public File getLicense() {
    return license;
  }

  @Nullable
  public File getIcon() {
    return icon;
  }

  public List<SignedFile> getFiles() {
    return files;
  }

  public static Builder builder(String name, String version) {
    return new Builder(name, version);
  }

  /**
   * Builder to create a Package.
   */
  public static class Builder {
    private static final Gson GSON = new Gson();
    private final String name;
    private final String version;
    private PackageMeta meta;
    private File license;
    private File icon;
    private SignedFile archive;
    private SignedFile spec;
    private List<SignedFile> files;

    public Builder(String name, String version) {
      this.name = name;
      this.version = version;
      this.files = new ArrayList<>();
    }

    public Builder setArchive(SignedFile archive) {
      this.archive = archive;
      return this;
    }

    public Builder setSpec(SignedFile spec) {
      this.spec = spec;
      try (Reader reader = new FileReader(spec.getFile())) {
        PackageSpec specObj = GSON.fromJson(reader, PackageSpec.class);
        specObj.validate();
        meta = PackageMeta.fromSpec(name, version, specObj);
      } catch (Exception e) {
        throw new IllegalArgumentException("Unable to parse spec file " + spec, e);
      }
      return this;
    }

    public Builder setLicense(File license) {
      this.license = license;
      return this;
    }

    public Builder setIcon(File icon) {
      this.icon = icon;
      return this;
    }

    public Builder addFile(SignedFile file) {
      this.files.add(file);
      return this;
    }

    public PackageMeta getMeta() {
      return meta;
    }

    public Package build() {
      return new Package(name, version, meta, license, icon, archive, spec, files);
    }
  }
}
